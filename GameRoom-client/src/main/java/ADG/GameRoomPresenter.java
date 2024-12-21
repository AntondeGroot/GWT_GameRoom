package ADG;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class GameRoomPresenter implements Presenter{

    private Timer playerPollingTimer;
    private final GameRoomView view;
    private Room model;
    private final PresenterManager presenterManager;

    public GameRoomPresenter(GameRoomView view, Room model, PresenterManager presenterManager) {
        this.view = view;
        this.model = model;
        this.presenterManager = presenterManager;
    }

    @Override
    public void start() {
        bind();
        pollServerForPlayers();
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

    public void pollServerForPlayers() {
        GWT.log("polling server for players");
//        gameRoomService.getRoomById(room.getId(), new AsyncCallback<Room>() {
//            @Override
//            public void onFailure(Throwable throwable) {
//            }
//
//            @Override
//            public void onSuccess(Room room) {
//                HashMap<String, String> serverUserNames = room.getUserNames();
//                HashMap<String, String> serverUserProfiles =  room.getUserProfiles();
//                if(!serverUserNames.equals(userNames) || !serverUserProfiles.equals(userProfiles)){
//                    userNames = serverUserNames;
//                    userProfiles = serverUserProfiles;
//                    drawPlayerList();
//                }
//                GWT.log("users = "+userNames.toString());
//            }
//        });
    }
}
