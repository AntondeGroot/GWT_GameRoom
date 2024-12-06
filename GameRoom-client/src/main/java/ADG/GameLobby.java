package ADG;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class GameLobby implements EntryPoint {

    private VerticalPanel mainPanel = new VerticalPanel();
    private VerticalPanel roomListPanel = new VerticalPanel();
    private TextBox roomNameInput = new TextBox();
    private Button createRoomButton = new Button("Create Room");


    /**
     * Create a remote service proxy to talk to the server-side GameRoom service.
     */
    private final GameRoomServiceAsync gameRoomService = GWT.create(GameRoomService.class);

    public void onModuleLoad() {
        RootPanel.get().add(mainPanel);

        // Title
        Label title = new Label("Game Lobby");
//        title.setStyleName("title");
        mainPanel.add(title);

        // Room creation section
        HorizontalPanel createRoomPanel = new HorizontalPanel();
        createRoomPanel.add(new Label("Room Name:"));
        createRoomPanel.add(roomNameInput);
        createRoomPanel.add(createRoomButton);
        mainPanel.add(createRoomPanel);

        // Room list
        mainPanel.add(new Label("Available Rooms:"));
        mainPanel.add(roomListPanel);

        // Create room button click handler
        createRoomButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String roomName = roomNameInput.getText().trim();
                gameRoomService.createRoom(roomName, new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        Window.alert("Room name can not be empty or a duplicate");
                    }

                    @Override
                    public void onSuccess(String s) {
                        navigateToRoom(roomName);
                    }
                });
            }
        });

        Timer timer = new Timer() {
            @Override
            public void run() {
                pollServerForRooms();
            }
        };
        // Schedule the timer to run every 200ms
        timer.scheduleRepeating(200);
    }

    private void updateRoomList(RoomResponse roomResponse) {
        roomListPanel.clear();
        for (String roomName : roomResponse.getRoomNames()) {
            Button joinButton = new Button("Join");
            HorizontalPanel roomPanel = new HorizontalPanel();
            roomPanel.add(new Label(roomName));
            roomPanel.add(joinButton);
            roomListPanel.add(roomPanel);

            joinButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    GWT.log("navigate to room " + roomName);
                    navigateToRoom(roomName);
                }
            });
        }
    }

    private void navigateToRoom(String roomName) {
        RootPanel.get().clear();
        GameRoom gameRoom = new GameRoom(roomName);
        gameRoom.load();
    }

    public void pollServerForRooms(){

        gameRoomService.getRooms(new AsyncCallback<RoomResponse>(){
            @Override
            public void onFailure(Throwable throwable) {}

            @Override
            public void onSuccess(RoomResponse roomResponse) {
                updateRoomList(roomResponse);
            }
        });
    }
}
