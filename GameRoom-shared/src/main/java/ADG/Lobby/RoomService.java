package ADG.Lobby;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.ArrayList;


@RemoteServiceRelativePath("gameroom")
public interface RoomService extends RemoteService {
    ArrayList<Room> getRooms();
    Room getRoomById(String roomId);
    Room createRoom(Room room) throws IllegalArgumentException;
    void deleteRoom(String roomName);
    void updateRoom(Room room);
    void addPlayerIdToRoom(String playerId, Room room);
    void removePlayerFromRoom(String playerId, Room room);
    void setUsernameAndProfile(Room room, String userId, String username, String profileId);
    Room startGame(String roomId) throws IllegalArgumentException;
    ArrayList<GameDefinition> getAvailableGames();
}
