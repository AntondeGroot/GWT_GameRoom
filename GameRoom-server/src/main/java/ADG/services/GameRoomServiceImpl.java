package ADG.services;

import ADG.GameRoomService;
import ADG.RoomResponse;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;

@SuppressWarnings("serial")
@WebServlet("/app/gameroom")
public class GameRoomServiceImpl extends RemoteServiceServlet implements GameRoomService{

    @Override
    public RoomResponse getRooms() {
        ArrayList<String> rooms = new ArrayList<>();
        rooms.add("room a");
        rooms.add("room b");
        rooms.add("room c");

        RoomResponse gameRooms = new RoomResponse();
        gameRooms.setRoomNames(rooms);
        return gameRooms;
    }
}
