package ADG.Lobby;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

public interface GameRoomServiceAsync {
    void getRooms(AsyncCallback<ArrayList<Room>> asyncCallback)
            throws IllegalArgumentException;

    void getRoomById(String roomId, AsyncCallback<Room> asyncCallback)
        throws IllegalArgumentException;

    void createRoom(Room room, AsyncCallback<Room> asyncCallback)
            throws IllegalArgumentException;

    void deleteRoom(String name, AsyncCallback<Void> asyncCallback)
        throws IllegalArgumentException;

    void updateRoom(Room room, AsyncCallback<Void> asyncCallback)
        throws IllegalArgumentException;

    void addPlayerIdToRoom(String playerId, Room room, AsyncCallback<Void> asyncCallback)
            throws IllegalArgumentException;

    void removePlayerFromRoom(String playerId, Room room, AsyncCallback<Void> asyncCallback)
            throws IllegalArgumentException;

    void setUsernameAndProfile(Room room, String userId, String username, String profileId, AsyncCallback<Void> asyncCallback)
        throws IllegalArgumentException;

    void startGame(String roomId, AsyncCallback<Room> asyncCallback)
            throws IllegalArgumentException;
}
