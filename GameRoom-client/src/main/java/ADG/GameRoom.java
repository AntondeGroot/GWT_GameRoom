package ADG;


import com.google.gwt.user.client.ui.*;

public class GameRoom {

    private String roomName;

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
        });

        RootPanel.get().add(roomPanel);
    }
}

