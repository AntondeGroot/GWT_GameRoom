package ADG;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;

public class GameLobbyPresenter {

    public interface Display {
        Button getCreateRoomButton();
        TextBox getRoomNameInput();
        HTMLPanel getRoomTableContainer();
    }

    private final GameLobbyView view;
    private final GameRoomServiceAsync gameRoomService = GWT.create(GameRoomService.class);
    private final ArrayList<Room> rooms = new ArrayList<>();
    private Timer timer;

    public GameLobbyPresenter(GameLobbyView view) {
        this.view = view;
        bind();
    }

    private void bind() {
        view.getCreateRoomButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String roomName = view.getRoomNameInput().getText().trim();
                if (roomName.isEmpty()) {
                    Window.alert("Room name cannot be empty.");
                    return;
                }
                Room room = new Room(roomName, Cookie.getPlayerId());
                gameRoomService.createRoom(room, new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        Window.alert("Failed to create room: " + throwable.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        Window.alert("Room created successfully.");
                    }
                });
            }
        });
        initializeRoomTable();
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

    private void initializeRoomTable() {
        // Add Room Name Column
        TextColumn<Room> roomNameColumn = new TextColumn<Room>() {
            @Override
            public String getValue(Room room) {
                return room.getName();
            }
        };

        // Add Number of Players Column
        TextColumn<Room> nrPlayersColumn = new TextColumn<Room>() {
            @Override
            public String getValue(Room room) {
                return room.getNrOfPlayers() + " / 8";
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
        ButtonCell joinButtonCell = new StyledButtonCell("joinRoomButton");
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

        // Add columns to the table
        view.getRoomTable().addColumn(roomNameColumn, "Room Name");
        view.getRoomTable().addColumn(nrPlayersColumn, "Players");
        view.getRoomTable().addColumn(statusColumn, "Status");
        view.getRoomTable().addColumn(joinButtonColumn, "");
    }

    public void loadRooms() {
        gameRoomService.getRooms(new AsyncCallback<ArrayList<Room>>() {
            @Override
            public void onFailure(Throwable throwable) {
                Window.alert("Failed to fetch rooms: " + throwable.getMessage());
            }

            @Override
            public void onSuccess(ArrayList<Room> roomList) {
                rooms.clear();
                rooms.addAll(roomList);
                updateRoomTable();
            }
        });
    }

    private void updateRoomTable() {
        view.getRoomTable().setRowData(0, rooms);
        view.getRoomTable().setRowCount(rooms.size());
    }

    public void displayRooms() {
        gameRoomService.getRooms(new AsyncCallback<ArrayList<Room>>() {
            @Override
            public void onFailure(Throwable throwable) {
                Window.alert("Failed to fetch rooms: " + throwable.getMessage());
            }

            @Override
            public void onSuccess(ArrayList<Room> fetchedRooms) {
                rooms.clear();
                rooms.addAll(fetchedRooms);
                updateRoomTable();
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
            public void onSuccess(ArrayList<Room> fetchedRooms) {
                rooms.clear();
                rooms.addAll(fetchedRooms);
                updateRoomTable();
            }
        });
    }

    /**
     * Navigate to the selected room.
     */
    private void navigateToCharacterSelection(Room room) {
        RootPanel.get().clear();
        CharacterSelection characterSelection = new CharacterSelection(room);
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
}
