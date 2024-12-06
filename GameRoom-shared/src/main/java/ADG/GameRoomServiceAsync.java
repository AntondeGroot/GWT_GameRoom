package ADG;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GameRoomServiceAsync {
    void getRooms(AsyncCallback<RoomResponse> asyncCallback)
            throws IllegalArgumentException;

    void createRoom(String name, AsyncCallback<String> asyncCallback)
            throws IllegalArgumentException;

    void deleteRoom(String name, AsyncCallback<Void> asyncCallback)
        throws IllegalArgumentException;
}
