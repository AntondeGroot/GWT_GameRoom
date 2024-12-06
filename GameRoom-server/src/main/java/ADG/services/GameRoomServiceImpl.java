package ADG.services;

import ADG.GameRoomService;
import ADG.RoomResponse;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;

@SuppressWarnings("serial")
@WebServlet("/app/gameroom")
public class GameRoomServiceImpl extends RemoteServiceServlet implements GameRoomService{
    ArrayList<String> rooms = new ArrayList<>();

    @Override
    public RoomResponse getRooms() {
        RoomResponse gameRooms = new RoomResponse();
        gameRooms.setRoomNames(rooms);
        return gameRooms;
    }

    @Override
    public String createRoom(String name) throws IllegalArgumentException {
        if (rooms.contains(name) || name.isBlank()) {
            throw new IllegalArgumentException();
        }

        rooms.add(name);
        return name;
    }
}
