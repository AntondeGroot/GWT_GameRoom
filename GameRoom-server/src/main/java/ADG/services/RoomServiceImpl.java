package ADG.services;

import ADG.Lobby.GameDefinition;
import ADG.Lobby.GameStatus;
import ADG.Lobby.Room;
import ADG.Lobby.RoomService;
import ADG.config.GamesConfig;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.servlet.annotation.WebServlet;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("serial")
@WebServlet("/app/gameroom")
public class RoomServiceImpl extends RemoteServiceServlet implements RoomService {

    @Autowired
    private GamesConfig gamesConfig;

    private final RestTemplate restTemplate = new RestTemplate();
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

                GameDefinition game = gamesConfig.findById(room1.getGameId())
                        .orElseThrow(() -> new IllegalArgumentException("Unknown game: " + room1.getGameId()));

                String baseUrl = game.getBaseUrl();

                // 1. Create a game session
                Map<String, Object> newGameRequest = new HashMap<>();
                newGameRequest.put("roomName", room1.getName());
                newGameRequest.put("maxPlayers", room1.getPlayerNames().size());
                Map sessionResponse = restTemplate.postForObject(baseUrl + "/games", newGameRequest, Map.class);
                String sessionId = sessionResponse.get("sessionId").toString();

                // 2. Add each player — reuse their GameRoom playerId so no mapping is needed
                for (Map.Entry<String, String> entry : room1.getPlayerNames().entrySet()) {
                    String playerId = entry.getKey();
                    String playerName = entry.getValue();
                    String profilePic = room1.getPlayerProfiles().get(playerId);
                    Map<String, Object> playerRequest = new HashMap<>();
                    playerRequest.put("id", playerId);
                    playerRequest.put("name", playerName);
                    playerRequest.put("profilePic", profilePic);
                    restTemplate.postForObject(baseUrl + "/games/" + sessionId + "/players", playerRequest, Map.class);
                }

                // 3. Start the game
                restTemplate.postForObject(baseUrl + "/games/" + sessionId + "/", null, Void.class);

                // 4. Store session info and mark room as playing
                room1.setGameSessionId(sessionId);
                room1.setGameBaseUrl(baseUrl);
                room1.setStatus(GameStatus.PLAYING);
                return room1;
            }
        }
        return null;
    }

    @Override
    public ArrayList<GameDefinition> getAvailableGames() {
        ArrayList<GameDefinition> reachable = new ArrayList<>();
        for (GameDefinition game : gamesConfig.getAvailable()) {
            if (isReachable(game.getBaseUrl())) {
                reachable.add(game);
            }
        }
        return reachable;
    }

    private boolean isReachable(String baseUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl).openConnection();
            connection.setConnectTimeout(1500);
            connection.setReadTimeout(1500);
            connection.setRequestMethod("HEAD");
            int code = connection.getResponseCode();
            return code < 500;
        } catch (Exception e) {
            return false;
        }
    }

}
