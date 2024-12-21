package ADG;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.HashMap;

public class GameRoomPresenter implements Presenter{

    private final GameRoomServiceAsync gameRoomService = GWT.create(GameRoomService.class);
    private Timer playerPollingTimer;
    private final GameRoomView view;
    private Room room;
    private final PresenterManager presenterManager;
    private HashMap<String, String> userNames = new HashMap<>();
    private HashMap<String, String> userProfiles = new HashMap<>();

    public GameRoomPresenter(GameRoomView view, Room model, PresenterManager presenterManager) {
        this.view = view;
        this.room = model;
        this.presenterManager = presenterManager;
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
        view.getDeleteRoomButton().setVisible(displayDeleteButton());
    }

    private void leaveRoom() {
        RootPanel.get().clear();
//        gameRoomService.removePlayerFromRoom(playerId, room, new AsyncCallback<Void>() {
//            @Override
//            public void onFailure(Throwable throwable) {
//                // Handle failure
//            }
//
//            @Override
//            public void onSuccess(Void aVoid) {
//                // Successfully removed player from room
//            }
//        });
        removePlayerFromRoom();
        presenterManager.switchToLobby();
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

    public boolean displayDeleteButton(){
        if(!room.getCreatedByUserId().equals(Cookie.getPlayerId())){
            return false;
        }
        if(room.getStatus() == GameStatus.PLAYING){
            return false;
        }
        return true;
    }

    public void startPollingServer(){
        // Poll the server every 2 seconds for updated room list
        if(playerPollingTimer == null){
            playerPollingTimer = new Timer() {
                @Override
                public void run() {
                    pollServerForPlayers();
                }
            };
            playerPollingTimer.scheduleRepeating(500);
        }
    }

    public void pollServerForPlayers() {
        GWT.log("polling server for players");
        gameRoomService.getRoomById(room.getId(), new AsyncCallback<Room>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log(throwable.getMessage());
            }

            @Override
            public void onSuccess(Room room) {
                HashMap<String, String> serverUserNames = room.getPlayerNames();
                HashMap<String, String> serverUserProfiles =  room.getPlayerProfiles();
                if(!serverUserNames.equals(userNames)){
                    // || !serverUserProfiles.equals(userProfiles)
                    userNames = serverUserNames;
                    userProfiles = serverUserProfiles;
                    GWT.log("drawing player names: "+serverUserNames);
                    drawPlayerList(userNames, userProfiles);
                }
                GWT.log(""+!serverUserNames.equals(userNames));
                GWT.log("servernames: "+serverUserNames);
                GWT.log("users = " + userNames.toString());
                GWT.log("Room = " + room);
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

    public void drawPlayerList(HashMap<String, String> userNames, HashMap<String, String> userProfiles) {
        view.getPlayerPanel().clear();
        for (String userId : userNames.keySet()) {
            HorizontalPanel playerIndexPanel = new HorizontalPanel();
            String playerName = userNames.get(userId);
            String userProfile = userProfiles.get(userId);
            playerIndexPanel.add(new Label(playerName));
            playerIndexPanel.add(new Label(".  -  ."));
            playerIndexPanel.add(new Label(userProfile));
            view.getPlayerPanel().add(playerIndexPanel);
        }
    }
}
