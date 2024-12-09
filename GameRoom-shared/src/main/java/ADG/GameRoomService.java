package ADG;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.ArrayList;


@RemoteServiceRelativePath("gameroom")
public interface GameRoomService extends RemoteService {
    ArrayList<Room> getRooms();
    Room getRoom(String roomName);
    String createRoom(Room room);
    void deleteRoom(String roomName);
    void addPlayerToRoom(String playerId, Room room);
    void removePlayerFromRoom(String playerId, Room room);
}
