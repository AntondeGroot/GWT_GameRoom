package ADG.Lobby;

import ADG.*;
import ADG.Utils.Cookie;
import ADG.Utils.PollingService;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

public class LobbyPresenter implements Presenter {

    private static final int POLLING_INTERVAL_MS = 200;
    private final LobbyView view;
    private final PresenterManager presenterManager;
    private final RoomServiceAsync roomService;
    private final ArrayList<Room> rooms = new ArrayList<>();
    private Timer roomPollingTimer;
    private boolean isInitialized = false;
    private PollingService pollingService = new PollingService();

    @Override
    public void start() {
        History.newItem("");
        bind();
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
            if (!isRoomNameValid(roomName)) {
                view.showAlert("Room name cannot be empty.");
                return;
            }
            createRoom(roomName);
        });
        initializeRoomTableHeaders();
    }

    private void initializeRoomTableHeaders() {
        if(!isInitialized){
            isInitialized = true;
            view.initializeTableHeaders();
            // Set the action for the join button
            view.getRoomTable().getColumn(3).setFieldUpdater((index, room, value) -> navigateToCharacterSelection(room));
        }
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
        return roomName != null && !roomName.trim().isEmpty();
    }

    private void createRoom(String roomName) {
        Room room = new Room(roomName, Cookie.getPlayerId());
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
