package ADG;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GameRoomPresenter implements Presenter{

    private final GameRoomServiceAsync gameRoomService;
    private final MessageServiceAsync messageService;
    private Timer playerPollingTimer;
    private final GameRoomView view;
    private ArrayList<Message> storedMessages = new ArrayList<>();
    private Room room;
    private final PresenterManager presenterManager;
    private HashMap<String, String> userNames = new HashMap<>();
    private HashMap<String, String> userProfiles = new HashMap<>();

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
        startPollingServer();
    }

    @Override
    public void stop() {
        // Stop any timers and clear the event handlers
        if (playerPollingTimer != null) {
            playerPollingTimer.cancel();
            playerPollingTimer = null;
        }
    }
    
    private void bind(){
        view.getLeaveRoomButton().addClickHandler(event -> leaveRoom());
        view.getDeleteRoomButton().addClickHandler(event -> deleteRoom());
        view.getDeleteRoomButton().setVisible(displayButtonForCreatorOfRoom());
        view.getDeleteRoomButton().setEnabled(displayButtonForCreatorOfRoom());
        view.getStartGameButton().addClickHandler(event -> startGame());
        view.getStartGameButton().setVisible(displayButtonForCreatorOfRoom());
        view.getStartGameButton().setEnabled(displayButtonForCreatorOfRoom());
        view.getSendMessageButton().addClickHandler(event -> sendMessage());
    }

    private void startGame() {
        gameRoomService.startGame(room.getId(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {

            }

            @Override
            public void onSuccess(Void unused) {
                view.getStartGameButton().setVisible(false);
                view.getStartGameButton().setEnabled(false);
                view.getDeleteRoomButton().setVisible(false);
                view.getDeleteRoomButton().setEnabled(false);
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
        RootPanel.get().clear();
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
//            gameRoomService.deleteRoom(room.getName(), new AsyncCallback<Void>() {
//                @Override
//                public void onFailure(Throwable throwable) {
//                    // Handle failure
//                }
//
//                @Override
//                public void onSuccess(Void unused) {
//                    RootPanel.get().clear();
//                    presenterManager.switchToLobby();
//                }
//            });
        }
    }

    public boolean displayButtonForCreatorOfRoom(){
        if(!room.getCreatedByUserId().equals(Cookie.getPlayerId())){
            return false;
        }
        if(room.getStatus() == GameStatus.PLAYING){
            return false;
        }
        return true;
    }

    public void startPollingServer(){
        // Poll the server every 0.5 seconds for updated room list
        if(playerPollingTimer == null){
            playerPollingTimer = new Timer() {
                @Override
                public void run() {
                    pollServerForPlayers();
                    pollServerForMessages();
                }
            };
            playerPollingTimer.scheduleRepeating(500);
        }
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
                    GWT.log("updating users");
                    userNames = serverUserNames;
                    userProfiles = serverUserProfiles;
                    GWT.log("drawing player names: "+serverUserNames);
                    drawPlayerList();
                }
                GWT.log(""+!serverUserNames.equals(userNames));
                GWT.log("servernames: "+serverUserNames);
                GWT.log("users = " + userNames.toString());
                GWT.log("Room = " + room);
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
                    StringBuilder output = new StringBuilder();
                    String lastTimeStamp = "";
                    for(Message message : fetchedMessages){
                        if(!lastTimeStamp.equals(message.getTimestamp())){
                            lastTimeStamp = message.getTimestamp();
                            output.append(lastTimeStamp);
                        }else{
                            output.append("     ");
                        }
                        output.append(" ").append(message.getNameSender()).append(" : ").append(message.getMessage());
                        output.append("\n");
                    }
                    view.getMessageDisplayField().setText(output.toString());
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

    public void drawPlayerList() {
        view.getPlayerPanel().clear();
        for (String userId : userNames.keySet()) {
            HorizontalPanel playerIndexPanel = new HorizontalPanel();
            String playerName = userNames.get(userId);
            String playerProfileUrl = userProfiles.get(userId);
            // create image
            Image profilePic = new Image(playerProfileUrl);
            profilePic.setStyleName("profile-pic-small");
            profilePic.setWidth("50px");
            // create player label
            playerIndexPanel.add(profilePic);
            Label playerNameLabel = new Label(playerName);
            playerIndexPanel.add(playerNameLabel);

            view.getPlayerPanel().add(playerIndexPanel);
        }
    }

    public static String getCurrentTime() {
        // Get the current date and time
        Date currentDate = new Date();

        // Format the time as "HH:mm"
        DateTimeFormat timeFormat = DateTimeFormat.getFormat("HH:mm");

        // Return the formatted time as a string
        return timeFormat.format(currentDate);
    }
}
