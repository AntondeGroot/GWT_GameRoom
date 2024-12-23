package ADG;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.HashMap;

import static ADG.TimeUtils.getCurrentTime;

public class GameRoomPresenter implements Presenter{

    private final GameRoomServiceAsync gameRoomService;
    private final MessageServiceAsync messageService;
    private final GameRoomView view;
    private ArrayList<Message> storedMessages = new ArrayList<>();
    private Room room;
    private final PresenterManager presenterManager;
    private HashMap<String, String> userNames = new HashMap<>();
    private HashMap<String, String> userProfiles = new HashMap<>();
    private PollingService pollingService = new PollingService();

    public GameRoomPresenter(GameRoomView view, Room model, PresenterManager presenterManager, GameRoomServiceAsync gameRoomService, MessageServiceAsync messageService) {
        this.view = view;
        this.room = model;
        this.presenterManager = presenterManager;
        this.gameRoomService = gameRoomService;
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
        view.getLeaveRoomButton().addClickHandler(event -> leaveRoom());
        view.getDeleteRoomButton().addClickHandler(event -> deleteRoom());
        view.getStartGameButton().addClickHandler(event -> startGame());
        view.getSendMessageButton().addClickHandler(event -> sendMessage());

        view.updateCreatorControls(room);
    }

    private void startGame() {
        gameRoomService.startGame(room.getId(), new AsyncCallback<Room>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Room room) {
                view.updateCreatorControls(room);
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
        String inputText = view.getMessageInputField().getText();
        if(!inputText.isEmpty()){
            sendMessageToServer(inputText);
            view.getMessageInputField().setText("");
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
            gameRoomService.deleteRoom(room.getName(), new AsyncCallback<Void>() {
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
        gameRoomService.getRoomById(room.getId(), new AsyncCallback<Room>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log(throwable.getMessage());
            }

            @Override
            public void onSuccess(Room room) {
                HashMap<String, String> serverUserNames = room.getPlayerNames();
                HashMap<String, String> serverUserProfiles =  room.getPlayerProfiles();
                if(!serverUserNames.equals(userNames) || !serverUserProfiles.equals(userProfiles)){
                    userNames = serverUserNames;
                    userProfiles = serverUserProfiles;
                    view.refreshPlayerList(userNames, userProfiles);
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
                    view.refreshMessages(fetchedMessages);
                }
            }
        });
    }

    private void removePlayerFromRoom() {
        gameRoomService.removePlayerFromRoom(Cookie.getPlayerId(), room, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Void v) {
            }
        });
    }
}
