package ADG.services;

import ADG.Lobby.GameDefinition;
import ADG.Lobby.GameStatus;
import ADG.Lobby.Room;
import ADG.Lobby.RoomService;
import ADG.config.GamesConfig;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.annotation.WebServlet;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("serial")
@WebServlet("/app/gameroom")
public class RoomServiceImpl extends RemoteServiceServlet implements RoomService {

    @Autowired
    private GamesConfig gamesConfig;

    private static final long EMPTY_ROOM_TTL_MS = 15 * 60 * 1000L;

    private final RestTemplate restTemplate = new RestTemplate();
    private ArrayList<Room> rooms = new ArrayList<>();
    final ConcurrentHashMap<String, Long> emptyRoomTimestamps = new ConcurrentHashMap<>();

    @PostConstruct
    private void seedTestRoom() {
        // Valid profile indices (0-15, excluding 5 and 9)
        String[][] players = {
            {"Alice",   "0"},
            {"Bob",     "3"},
            {"Charlie", "6"},
            {"Diana",   "1"},
            {"Eve",     "12"},
            {"Frank",   "8"},
            {"Grace",   "14"},
        };

        Room room = new Room();
        room.setId(java.util.UUID.randomUUID().toString());
        room.setName("Test Room");
        room.setStatus(GameStatus.WAITING);
        room.setGameId("keezen");
        room.setMinPlayers(2);

        for (String[] p : players) {
            String pid = java.util.UUID.randomUUID().toString();
            room.addPlayer(pid);
            room.addPlayerName(pid, p[0]);
            room.addPlayerProfile(pid, p[1]);
        }
        room.setCreatedByUserId(room.getPlayerIds().get(0));
        rooms.add(room);
    }

    @Override
    public synchronized ArrayList<Room> getRooms() {
        ArrayList<Room> visible = new ArrayList<>();
        for (Room r : rooms) {
            if (r.getStatus() != GameStatus.PENDING) {
                visible.add(r);
            }
        }
        return visible;
    }

    @Override
    public synchronized Room getRoomById(String roomId){
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
    public synchronized Room createRoom(Room room) throws IllegalArgumentException {
        if (room.getName().isBlank() || room.getName().trim().length() < 3) {
            throw new IllegalArgumentException("Room name must be at least 3 characters.");
        }
        boolean nameAlreadyExists = rooms.stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(room.getName().trim()));
        if (nameAlreadyExists) {
            throw new IllegalArgumentException("A room with this name already exists.");
        }
        rooms.stream()
                .filter(r -> r.getPlayerIds().contains(room.getCreatedByUserId()))
                .findFirst()
                .ifPresent(r -> removePlayerFromRoom(room.getCreatedByUserId(), r.getId()));

        gamesConfig.findById(room.getGameId()).ifPresent(game -> {
            room.setMinPlayers(game.getMinPlayers());
            room.setMaxPlayers(game.getMaxPlayers());
        });
        room.setStatus(GameStatus.PENDING);
        rooms.add(room);
        return room;
    }

    @Override
    public synchronized void publishRoom(String roomId) {
        rooms.stream()
                .filter(r -> r.getId().equals(roomId) && r.getStatus() == GameStatus.PENDING)
                .findFirst()
                .ifPresent(r -> r.setStatus(GameStatus.WAITING));
    }

    @Override
    public synchronized void deleteRoom(String roomId) {
        Optional<Room> result = rooms.stream().filter(room -> room.getId().equals(roomId)).findFirst();
        if(result.isPresent()) {
            Room foundRoom = result.get();
            rooms.remove(foundRoom);
        }
    }

    @Override
    public synchronized void updateRoom(Room room){
        Optional<Room> result = rooms.stream().filter(r -> r.getId().equals(room.getId())).findFirst();
        if(result.isPresent()) {
            Room foundRoom = result.get();
            rooms.remove(foundRoom);
            rooms.add(room);
        }
    }

    @Override
    public synchronized void addPlayerIdToRoom(String playerId, String roomId) {
        boolean alreadyInAnotherRoom = rooms.stream()
                .anyMatch(r -> !r.getId().equals(roomId) && r.getPlayerIds().contains(playerId));
        if (alreadyInAnotherRoom) return;

        for (Room room1 : rooms) {
            if (room1.getId().equals(roomId)) {
                room1.addPlayer(playerId);
                emptyRoomTimestamps.remove(room1.getId());
                if (room1.getNrOfPlayers() >= room1.getMaxPlayers()) {
                    room1.setStatus(GameStatus.FULL);
                }
            }
        }
    }

    @Override
    public synchronized void removePlayerFromRoom(String playerId, String roomId) {
        for (Room room1 : rooms) {
            if (room1.getId().equals(roomId)) {
                room1.removePlayer(playerId);
                if (room1.getStatus() == GameStatus.FULL && room1.getNrOfPlayers() < room1.getMaxPlayers()) {
                    room1.setStatus(GameStatus.WAITING);
                }
                if (playerId.equals(room1.getCreatedByUserId()) && !room1.getPlayerIds().isEmpty()) {
                    String newCreator = room1.getPlayerIds().get(0);
                    room1.setCreatedByUserId(newCreator);
                }
                if (room1.getPlayerIds().isEmpty() && room1.getStatus() != GameStatus.PENDING) {
                    emptyRoomTimestamps.put(room1.getId(), System.currentTimeMillis());
                }
            }
        }
    }

    @Override
    public synchronized void setUsernameAndProfile(Room room, String userId, String username, String profileId) {
        for (Room room1 : rooms) {
            if (room1.getId().equals(room.getId())) {
                room1.addPlayerName(userId, username);
                room1.addPlayerProfile(userId, profileId);
            }
        }
    }

    @Override
    public synchronized Room startGame(String roomId) {
        for (Room room1 : rooms) {
            if (room1.getId().equals(roomId)) {

                GameDefinition game = gamesConfig.findById(room1.getGameId())
                        .orElseThrow(() -> new IllegalArgumentException("Unknown game: " + room1.getGameId()));

                String baseUrl = game.getBaseUrl();

                // 0. Enforce minimum players
                if (room1.getPlayerNames().size() < game.getMinPlayers()) {
                    throw new IllegalArgumentException(
                            "Not enough players. Need at least " + game.getMinPlayers()
                            + ", but only " + room1.getPlayerNames().size() + " have joined.");
                }

                // 1. Create a game session
                Map<String, Object> newGameRequest = new HashMap<>();
                newGameRequest.put("roomName", room1.getName());
                newGameRequest.put("maxPlayers", room1.getPlayerNames().size());
                Map sessionResponse;
                try {
                    sessionResponse = restTemplate.postForObject(baseUrl + "/games", newGameRequest, Map.class);
                } catch (RestClientException e) {
                    throw new IllegalArgumentException("Could not reach game server at " + baseUrl + ": " + e.getMessage());
                }
                String sessionId = sessionResponse.get("sessionId").toString();

                // 2. Add each player & 3. Start the game — clean up the session on any failure
                try {
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
                } catch (RestClientException e) {
                    // Attempt to clean up the orphaned session on the game server
                    try {
                        restTemplate.delete(baseUrl + "/games/" + sessionId);
                    } catch (RestClientException ignored) {}
                    throw new IllegalArgumentException("Failed to start game session: " + e.getMessage());
                }

                // 4. Store session info and mark room as playing
                room1.setGameSessionId(sessionId);
                room1.setGameBaseUrl("/" + game.getId());
                room1.setStatus(GameStatus.PLAYING);
                return room1;
            }
        }
        return null;
    }

    @Override
    public synchronized ArrayList<GameDefinition> getAvailableGames() {
        ArrayList<GameDefinition> reachable = new ArrayList<>();
        for (GameDefinition game : gamesConfig.getAvailable()) {
            if (isReachable(game.getHealthUrl())) {
                reachable.add(game);
            }
        }
        return reachable;
    }

    @Scheduled(fixedDelay = 60_000)
    public synchronized void deleteEmptyRooms() {
        long now = System.currentTimeMillis();
        emptyRoomTimestamps.entrySet().removeIf(entry -> {
            if (now - entry.getValue() >= EMPTY_ROOM_TTL_MS) {
                rooms.removeIf(r -> r.getId().equals(entry.getKey()) && r.getPlayerIds().isEmpty());
                return true;
            }
            return false;
        });
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
