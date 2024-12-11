package ADG;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;

import java.util.ArrayList;
import java.util.List;

public class GameLobby implements EntryPoint {

    private VerticalPanel mainPanel = new VerticalPanel();
    private TextBox roomNameInput = new TextBox();
    private Button createRoomButton = new Button("Create Room");
    private ArrayList<Room> rooms = new ArrayList<>();
    private CellTable<Room> roomTable = new CellTable<>();
    private ListDataProvider<Room> dataProvider = new ListDataProvider<>();
    private Timer timer;

    /**
     * Create a remote service proxy to talk to the server-side GameRoom service.
     */
    private final GameRoomServiceAsync gameRoomService = GWT.create(GameRoomService.class);

    public void onModuleLoad() {
        String playerid = Cookie.getPlayerId();

        RootPanel.get().add(mainPanel);

        // Title
        Label title = new Label("Game Lobby");
        mainPanel.add(title);

        // Room creation section
        HorizontalPanel createRoomPanel = new HorizontalPanel();
        createRoomPanel.add(new Label("Room Name:"));
        createRoomPanel.add(roomNameInput);
        createRoomPanel.add(createRoomButton);
        mainPanel.add(createRoomPanel);

        // Room list title
        Label availableRoomsHeader = new Label("Available Rooms:");
        mainPanel.add(availableRoomsHeader);

        // Setup the room table
        setupRoomTable();

        mainPanel.add(roomTable);

        // Create room button click handler
        createRoomButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String roomName = roomNameInput.getText().trim();
                if (roomName.isEmpty()) {
                    Window.alert("Room name cannot be empty.");
                    return;
                }
                Room room = new Room(roomName, playerid);
                gameRoomService.createRoom(room, new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        Window.alert("Failed to create room: " + throwable.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        navigateToCharacterSelection(room);
                    }
                });
            }
        });

        // Poll the server every 0.2 seconds for updated room list
        if(timer == null){
            Timer timer = new Timer() {
                @Override
                public void run() {
                    pollServerForRooms();
                }
            };
            timer.scheduleRepeating(200);
        }
    }
    /**
     * Set up the CellTable to display the list of available rooms.
     */
    private void setupRoomTable() {
        // Add Room Name Column
        TextColumn<Room> roomNameColumn = new TextColumn<Room>() {
            @Override
            public String getValue(Room room) {
                return room.getName();
            }
        };

        // Add Room Name Column
        TextColumn<Room> nrPlayersColumn = new TextColumn<Room>() {
            @Override
            public String getValue(Room room) {
                return ""+room.getNrOfPlayers() + " / 8";
            }
        };

        // Add Status Column
        TextColumn<Room> statusColumn = new TextColumn<Room>() {
            @Override
            public String getValue(Room room) {
                return "waiting for players ...";
            }
        };

        // Add Join Button Column
        ButtonCell joinButtonCell = new ButtonCell();
        Column<Room, String> joinButtonColumn =
                new Column<Room, String>(joinButtonCell) {
                    @Override
                    public String getValue(Room room) {
                        return "Join";
                    }
                };

        // Set the action for the join button
        joinButtonColumn.setFieldUpdater((index, room, value) -> {
            GWT.log("Navigating to room: " + room.getName());
            navigateToCharacterSelection(room);
        });

        // Add the columns to the table
        roomTable.addColumn(roomNameColumn, "Room Name");
        roomTable.addColumn(nrPlayersColumn, "Nr Players");
        roomTable.addColumn(statusColumn, "Status");
        roomTable.addColumn(joinButtonColumn, "");

        // Add the data provider, which will automatically update the table
        dataProvider.addDataDisplay(roomTable);
    }

    /**
     * Update the room list by refreshing the ListDataProvider for the CellTable.
     */
    private void updateRoomList(ArrayList<Room> roomsResponse) {
        if (!rooms.equals(roomsResponse)) {
            GWT.log("Updating room list...");
            GWT.log("Old Rooms: " + rooms.toString());
            GWT.log("New Rooms: " + roomsResponse.toString());

            rooms = roomsResponse;

            // Clear and update the data in the ListDataProvider
            List<Room> list = dataProvider.getList();
            list.clear();
            list.addAll(roomsResponse);
        }
    }

    /**
     * Navigate to the selected room.
     */
    private void navigateToCharacterSelection(Room room) {
        RootPanel.get().clear();
        CharacterSelection characterSelection = new CharacterSelection(room.getName());
        characterSelection.load();
        gameRoomService.addPlayerIdToRoom(Cookie.getPlayerId(), room, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Void v) {
            }
        });
    }

    /**
     * Poll the server to get the list of available rooms.
     */
    public void pollServerForRooms() {
        gameRoomService.getRooms(new AsyncCallback<ArrayList<Room>>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log("Failed to fetch rooms: " + throwable.getMessage());
            }

            @Override
            public void onSuccess(ArrayList<Room> rooms) {
                updateRoomList(rooms);
            }
        });
    }
}
