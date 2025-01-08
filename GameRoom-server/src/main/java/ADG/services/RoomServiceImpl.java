package ADG.services;

import ADG.Lobby.RoomService;
import ADG.Lobby.GameStatus;
import ADG.Lobby.Room;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;
import java.util.Optional;

@SuppressWarnings("serial")
@WebServlet("/app/gameroom")
public class RoomServiceImpl extends RemoteServiceServlet implements RoomService {
    private ArrayList<Room> rooms = new ArrayList<>();

    @Override
    public ArrayList<Room> getRooms() {
        return rooms;
    }

    @Override
    public Room getRoomById(String roomId){
        Optional<Room> result = rooms.stream().filter(room -> room.getId().equals(roomId)).findFirst();
        if(result.isPresent()) {
            if(result.get().getId() == null){
                throw new IllegalArgumentException();
            }
            return result.get();
        }
        return null;
    }

    @Override
    public Room createRoom(Room room) throws IllegalArgumentException {
        if (rooms.contains(room) || room.getName().isBlank()) {
            throw new IllegalArgumentException();
        }

        rooms.add(room);
        return room;
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
    public void updateRoom(Room room){
        Optional<Room> result = rooms.stream().filter(r -> r.getName().equals(room.getName())).findFirst();
        if(result.isPresent()) {
            Room foundRoom = result.get();
            rooms.remove(foundRoom);
            rooms.add(room);
        }
    }

    @Override
    public void addPlayerIdToRoom(String playerId, Room room) {
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

    @Override
    public void setUsernameAndProfile(Room room, String userId, String username, String profileId) {
        for (Room room1 : rooms) {
            if (room1.getName().equals(room.getName())) {
                room1.addPlayerName(userId, username);
                room1.addPlayerProfile(userId, profileId);
            }
        }
    }

    @Override
    public Room startGame(String roomId) {
        for (Room room1 : rooms) {
            if (room1.getId().equals(roomId)) {
                room1.setStatus(GameStatus.PLAYING);
                return room1;
            }
        }
        return null;
    }

}
