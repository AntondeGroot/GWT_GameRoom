package ADG.Lobby;

import ADG.*;
import ADG.Utils.Cookie;
import ADG.Utils.PollingService;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.HashMap;

import static ADG.Utils.TimeUtils.getCurrentTime;

public class RoomPresenter implements Presenter {

    private final RoomServiceAsync roomService;
    private final MessageServiceAsync messageService;
    private final RoomView roomView;
    private ArrayList<Message> storedMessages = new ArrayList<>();
    private final Room room;
    private final PresenterManager presenterManager;
    private HashMap<String, String> userNames = new HashMap<>();
    private HashMap<String, String> userProfiles = new HashMap<>();
    private final PollingService pollingService = new PollingService();

    public RoomPresenter(RoomView roomView, Room model, PresenterManager presenterManager, RoomServiceAsync roomService, MessageServiceAsync messageService) {
        this.roomView = roomView;
        this.room = model;
        this.presenterManager = presenterManager;
        this.roomService = roomService;
        this.messageService = messageService;
    }

    @Override
    public void start() {
        History.newItem("room=" + room.getId());
        bind();
        pollingService.startPolling(500, this::pollServerForUpdates);
    }

    @Override
    public void stop() {
        pollingService.stopPolling();
    }
    
    private void bind(){
        roomView.showRoomName(room.getName());
        roomView.getLeaveRoomButton().addClickHandler(event -> leaveRoom());
        roomView.getDeleteRoomButton().addClickHandler(event -> deleteRoom());
        roomView.getStartGameButton().addClickHandler(event -> startGame());
        roomView.getSendMessageButton().addClickHandler(event -> sendMessage());

        roomView.updateCreatorControls(room);
    }

    private void startGame() {
        roomService.startGame(room.getId(), new AsyncCallback<Room>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Room room) {
                roomView.updateCreatorControls(room);
            }
        });
    }

    private void sendMessageToServer(String inputText){
        Message message = new Message(getCurrentTime(), userNames.get(Cookie.getPlayerId()), inputText);
        messageService.sendMessage(room.getId(), message, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Void v) {
            }
        });
    }

    private void sendMessage() {
        String inputText = roomView.getMessageInputField().getText();
        if(!inputText.isEmpty()){
            sendMessageToServer(inputText);
            roomView.getMessageInputField().setText("");
        }
    }

    private void leaveRoom() {
        removePlayerFromRoom();
        sendLeaveMessage();
        presenterManager.switchToLobby();
    }

    private void sendLeaveMessage() {
        sendMessageToServer("has left the room");
    }

    private void deleteRoom() {
        boolean confirmDelete = Window.confirm("Are you sure you want to delete this room?");
        if (confirmDelete) {
            roomService.deleteRoom(room.getName(), new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable throwable) {
                }

                @Override
                public void onSuccess(Void unused) {
                    presenterManager.switchToLobby();
                }
            });
        }
    }

    public void pollServerForUpdates(){
        pollServerForPlayers();
        pollServerForMessages();
    }

    public void pollServerForPlayers() {
        roomService.getRoomById(room.getId(), new AsyncCallback<Room>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log(throwable.getMessage());
            }

            @Override
            public void onSuccess(Room updatedRoom) {
                if (updatedRoom.getStatus() == GameStatus.PLAYING && updatedRoom.getGameSessionId() != null) {
                    stop();
                    String url = updatedRoom.getGameBaseUrl()
                            + "/?sessionid=" + updatedRoom.getGameSessionId()
                            + "&playerid=" + Cookie.getPlayerId();
                    Window.Location.replace(url);
                    return;
                }
                HashMap<String, String> serverUserNames = updatedRoom.getPlayerNames();
                HashMap<String, String> serverUserProfiles = updatedRoom.getPlayerProfiles();
                if (!serverUserNames.equals(userNames) || !serverUserProfiles.equals(userProfiles)) {
                    userNames = serverUserNames;
                    userProfiles = serverUserProfiles;
                    roomView.refreshPlayerList(userNames, userProfiles);
                }
            }
        });
    }

    public void pollServerForMessages() {
        messageService.getMessages(room.getId(), new AsyncCallback<ArrayList<Message>>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log(throwable.getMessage());
            }

            @Override
            public void onSuccess(ArrayList<Message> fetchedMessages) {
                if(!fetchedMessages.equals(storedMessages)){
                    storedMessages = fetchedMessages;
                    roomView.refreshMessages(fetchedMessages);
                }
            }
        });
    }

    private void removePlayerFromRoom() {
        roomService.removePlayerFromRoom(Cookie.getPlayerId(), room, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Void v) {
            }
        });
    }
}
