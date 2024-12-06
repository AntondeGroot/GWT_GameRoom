package ADG;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.ArrayList;

public interface GameRoomServiceAsync {
    void getRooms(AsyncCallback<RoomResponse> asyncCallback)
            throws IllegalArgumentException;
}
