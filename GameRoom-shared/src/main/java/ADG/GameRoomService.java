package ADG;

import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


@RemoteServiceRelativePath("gameroom")
public interface GameRoomService extends RemoteService {
    RoomResponse getRooms();
    String createRoom(String name);
}
