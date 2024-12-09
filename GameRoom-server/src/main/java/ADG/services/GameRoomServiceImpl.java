package ADG.services;

import ADG.GameRoomService;
import ADG.Room;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;
import java.util.Optional;

@SuppressWarnings("serial")
@WebServlet("/app/gameroom")
public class GameRoomServiceImpl extends RemoteServiceServlet implements GameRoomService{
    private ArrayList<Room> rooms = new ArrayList<>();

    @Override
    public ArrayList<Room> getRooms() {
//        RoomResponse gameRooms = new RoomResponse();
//        gameRooms.setRoomNames(rooms);
        return rooms;
    }

    @Override
    public Room getRoom(String roomName) {
        Optional<Room> result = rooms.stream().filter(room -> room.getName().equals(roomName)).findFirst();
        if(result.isPresent()) {
            return result.get();
        }
        return new Room();
    }

    @Override
    public String createRoom(Room room) throws IllegalArgumentException {
        if (rooms.contains(room) || room.getName().isBlank()) {
            throw new IllegalArgumentException();
        }

        rooms.add(room);
        return room.getName();
    }

    @Override
    public void deleteRoom(String roomName) {
        Optional<Room> result = rooms.stream().filter(room -> room.getName().equals(roomName)).findFirst();
        if(result.isPresent()) {
            Room foundRoom = result.get();
            rooms.remove(foundRoom);
        }
    }

    @Override
    public void addPlayerToRoom(String playerId, Room room) {
        for (Room room1 : rooms) {
            if (room1.getName().equals(room.getName())) {
                room1.addPlayer(playerId);
            }
        }
    }

    @Override
    public void removePlayerFromRoom(String playerId, Room room) {
        for (Room room1 : rooms) {
            if (room1.getName().equals(room.getName())) {
                room1.removePlayer(playerId);
            }
        }
    }
}
