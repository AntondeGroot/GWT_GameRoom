package ADG.Lobby;

import ADG.*;
import ADG.Utils.Cookie;
import ADG.Utils.PollingService;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

public class LobbyPresenter implements Presenter {

    private static final int POLLING_INTERVAL_MS = 200;
    private final LobbyView view;
    private final PresenterManager presenterManager;
    private final RoomServiceAsync roomService;
    private final ArrayList<Room> rooms = new ArrayList<>();
    private PollingService pollingService = new PollingService();

    @Override
    public void start() {
        History.newItem("");
        bind();
        loadAvailableGames();
        pollingService.startPolling(POLLING_INTERVAL_MS, this::pollServerForRooms);
    }

    @Override
    public void stop() {
        pollingService.stopPolling();
    }

    public LobbyPresenter(LobbyView view, PresenterManager presenterManager, RoomServiceAsync roomService) {
        this.view = view;
        this.presenterManager = presenterManager;
        this.roomService = roomService;
    }

    private void bind() {
        view.getCreateRoomButton().addClickHandler(event -> {
            String roomName = view.getRoomNameInput().getText().trim();
            if (roomName.isEmpty()) {
                view.showAlert("Room name cannot be empty.");
                return;
            }
            if (roomName.length() > 20) {
                view.showAlert("Room name cannot exceed 20 characters.");
                return;
            }
            createRoom(roomName);
        });
        view.setJoinHandler(room -> navigateToCharacterSelection(room));
    }

    private void updateRoomTable() {
        view.updateRoomTable(rooms);
    }

    /**
     * Poll the server to get the list of available rooms.
     */
    private void pollServerForRooms() {
        roomService.getRooms(new AsyncCallback<ArrayList<Room>>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log("Failed to fetch rooms: " + throwable.getMessage());
            }

            @Override
            public void onSuccess(ArrayList<Room> fetchedRooms) {
                if(!rooms.equals(fetchedRooms)){
                    GWT.log("update room list table");
                    updateRooms(fetchedRooms);
                    updateRoomTable();
                }
            }
        });
    }

    /**
     * Navigate to the selected room.
     */
    private void navigateToCharacterSelection(Room room) {
        presenterManager.switchToCharacterSelection(room);
        addPlayerIdToRoom(room);
    }

    private void addPlayerIdToRoom(Room room){
        roomService.addPlayerIdToRoom(Cookie.getPlayerId(), room, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log("Failed to add user to room");
            }

            @Override
            public void onSuccess(Void v) {
                GWT.log("Succesfully added user to room");
            }
        });
    }

    private boolean isRoomNameValid(String roomName) {
        return roomName != null && !roomName.trim().isEmpty() && roomName.trim().length() <= 20;
    }

    private void loadAvailableGames() {
        roomService.getAvailableGames(new AsyncCallback<ArrayList<GameDefinition>>() {
            @Override
            public void onFailure(Throwable throwable) {
                GWT.log("Failed to load available games: " + throwable.getMessage());
            }

            @Override
            public void onSuccess(ArrayList<GameDefinition> games) {
                view.populateGameList(games);
            }
        });
    }

    private void createRoom(String roomName) {
        String gameId = view.getSelectedGameId();
        if (gameId == null) {
            view.showAlert("Please select a game.");
            return;
        }
        Room room = new Room(roomName, Cookie.getPlayerId());
        room.setGameId(gameId);
        roomService.createRoom(room, new AsyncCallback<Room>() {
            @Override
            public void onFailure(Throwable throwable) {
                view.showAlert("Failed to create room: " + throwable.getMessage());
                GWT.log("Room creation failed", throwable);
            }

            @Override
            public void onSuccess(Room result) {
                navigateToCharacterSelection(room);
                view.getRoomNameInput().setText("");
            }
        });
    }

    private synchronized void updateRooms(ArrayList<Room> fetchedRooms) {
        rooms.clear();
        rooms.addAll(fetchedRooms);
    }
}
