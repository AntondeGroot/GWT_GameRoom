package ADG;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

public interface GameRoomServiceAsync {
    void getRooms(AsyncCallback<ArrayList<Room>> asyncCallback)
            throws IllegalArgumentException;

    void getRoom(String roomName, AsyncCallback<Room> asyncCallback)
        throws IllegalArgumentException;

    void createRoom(Room room, AsyncCallback<String> asyncCallback)
            throws IllegalArgumentException;

    void deleteRoom(String name, AsyncCallback<Void> asyncCallback)
        throws IllegalArgumentException;

    void addPlayerToRoom(String playerId, Room room, AsyncCallback<Void> asyncCallback)
            throws IllegalArgumentException;

    void removePlayerFromRoom(String playerId, Room room, AsyncCallback<Void> asyncCallback)
            throws IllegalArgumentException;
}
