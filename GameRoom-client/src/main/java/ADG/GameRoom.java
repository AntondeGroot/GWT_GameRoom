package ADG;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.HashMap;

public class GameRoom {
    private Room room;
    private String roomId;
    private String roomName;
    private Room gameroom;
    private Timer timer;
    private HashMap<String, String> userNames = new HashMap<>();
    private HashMap<String, String> userProfiles = new HashMap<>();

    private final GameRoomServiceAsync gameRoomService = GWT.create(GameRoomService.class);
    private VerticalPanel playerPanel = new VerticalPanel();

    public GameRoom(Room room) {
        this.room = room;
    }

    public void load() {
        VerticalPanel roomPanel = new VerticalPanel();

        // Room title
        History.newItem("room="+room.getId());
        Label roomTitle = new Label("Game Room: " + room.getName());
//        roomTitle.setStyleName("room-title");
        roomPanel.add(roomTitle);

        // Leave room button
        Button leaveRoomButton = new Button("Leave Room");
        roomPanel.add(leaveRoomButton);

        leaveRoomButton.addClickHandler(event -> {
            RootPanel.get().clear();
            GameLobby gameLobby = new GameLobby();
            gameLobby.onModuleLoad();
            removePlayerFromRoom();
        });

        // Delete room button
        // only display for the user that created it
        String playerId = Cookie.getPlayerId();
        gameRoomService.getRoomById(room.getId(), new AsyncCallback<Room>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                    }

                    @Override
                    public void onSuccess(Room room) {
                        gameroom = room;

                        // only display the delete button for the creator of the room
                        if(room.getCreatedByUserId().equals(playerId)){
                            Button deleteRoomButton = new Button("Delete Room");
                            roomPanel.add(deleteRoomButton);

                            deleteRoomButton.addClickHandler(event -> {
                                boolean deleteRoom = Window.confirm("Are you sure you want to delete this room?");
                                if(deleteRoom) {
                                    gameRoomService.deleteRoom(roomName, new AsyncCallback<Void>() {
                                        @Override
                                        public void onFailure(Throwable throwable) {}

                                        @Override
                                        public void onSuccess(Void unused) {
                                            RootPanel.get().clear();
                                            GameLobby gameLobby = new GameLobby();
                                            gameLobby.onModuleLoad();
                                        }
                                    });
                                }
                                RootPanel.get().add(roomPanel);
                            });
                        }
                    }
                });

        RootPanel.get().add(roomPanel);

        // Poll the server every 2 seconds for updated room list
        if(timer == null){
            Timer timer = new Timer() {
                @Override
                public void run() {
                    pollServerForPlayers();
                }
            };
            timer.scheduleRepeating(500);
        }

    }

    private void drawPlayerList() {
        GWT.log("drawPlayerList");

        for(String userId: userNames.keySet()){
            HorizontalPanel playerIndexPanel = new HorizontalPanel();

            String playerName = userNames.get(userId);
            String userProfile = userProfiles.get(userId);
            playerIndexPanel.add(new Label(playerName));
            playerIndexPanel.add(new Label(".  -  ."));
            playerIndexPanel.add(new Label(userProfile));

            playerPanel.add(playerIndexPanel);
        }
        RootPanel.get().add(playerPanel);
    }


    private void removePlayerFromRoom() {
        gameRoomService.getRoomById(room.getId(), new AsyncCallback<Room>() {

            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Room room) {
                gameRoomService.removePlayerFromRoom(Cookie.getPlayerId(), room, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                    }

                    @Override
                    public void onSuccess(Void v) {
                    }
                });
            }
        });


    }

    public void pollServerForPlayers() {
        GWT.log("polling server for players");
        gameRoomService.getRoomById(room.getId(), new AsyncCallback<Room>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Room room) {
                HashMap<String, String> serverUserNames = room.getUserNames();
                HashMap<String, String> serverUserProfiles =  room.getUserProfiles();
                if(!serverUserNames.equals(userNames) || !serverUserProfiles.equals(userProfiles)){
                    userNames = serverUserNames;
                    userProfiles = serverUserProfiles;
                    drawPlayerList();
                }
                GWT.log("users = "+userNames.toString());
            }
        });
    }

}

