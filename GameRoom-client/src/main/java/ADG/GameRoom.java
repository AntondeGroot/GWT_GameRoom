package ADG;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class GameRoom {

    private String roomName;

    private final GameRoomServiceAsync gameRoomService = GWT.create(GameRoomService.class);

    public GameRoom(String roomName) {
        this.roomName = roomName;
    }

    public void load() {
        VerticalPanel roomPanel = new VerticalPanel();

        // Room title
        Label roomTitle = new Label("Game Room: " + roomName);
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
        gameRoomService.getRoom(roomName, new AsyncCallback<Room>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                    }

                    @Override
                    public void onSuccess(Room room) {
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
    }


    private void removePlayerFromRoom() {
        gameRoomService.getRoom(roomName, new AsyncCallback<Room>() {

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
}

