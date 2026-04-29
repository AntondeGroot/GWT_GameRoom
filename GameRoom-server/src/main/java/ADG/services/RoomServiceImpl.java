package ADG.services;

import ADG.Lobby.GameDefinition;
import ADG.Lobby.GameOption;
import ADG.Lobby.GameStatus;
import ADG.Lobby.Room;
import ADG.Lobby.RoomService;
import ADG.Lobby.RoomServiceException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
@WebServlet("/app/gameroom")
public class RoomServiceImpl extends RemoteServiceServlet implements RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImpl.class);

    @Autowired
    private GamesConfig gamesConfig;

    @Autowired
    private RoomStore roomStore;

    private static final long EMPTY_ROOM_TTL_MS = 15 * 60 * 1000L;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    private void seedTestRoom() {
        String[][] players = {
            {"Alice",   "0"},
            {"Bob",     "15"},
            {"Charlie", "21"},
            {"Diana",   "23"},
            {"Eve",     "47"},
            {"Frank",   "52"},
            {"Grace",   "59"},
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
        roomStore.rooms.add(room);
    }

    @Override
    public synchronized ArrayList<Room> getRooms() {
        ArrayList<Room> visible = new ArrayList<>();
        for (Room r : roomStore.rooms) {
            if (r.getStatus() != GameStatus.PENDING) {
                visible.add(r);
            }
        }
        return visible;
    }

    @Override
    public synchronized Room getRoomById(String roomId){
        Optional<Room> result = roomStore.rooms.stream().filter(room -> room.getId().equals(roomId)).findFirst();
        if(result.isPresent()) {
            if(result.get().getId() == null){
                throw new IllegalArgumentException();
            }
            return result.get();
        }
        return null;
    }

    @Override
    public synchronized Room createRoom(Room room) throws RoomServiceException {
        if (room.getName().isBlank() || room.getName().trim().length() < 3) {
            throw new RoomServiceException("Room name must be at least 3 characters.");
        }
        boolean nameAlreadyExists = roomStore.rooms.stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(room.getName().trim()));
        if (nameAlreadyExists) {
            throw new RoomServiceException("A room with this name already exists.");
        }
        roomStore.rooms.stream()
                .filter(r -> r.getPlayerIds().contains(room.getCreatedByUserId()))
                .findFirst()
                .ifPresent(r -> removePlayerFromRoom(room.getCreatedByUserId(), r.getId()));

        gamesConfig.findById(room.getGameId()).ifPresent(game -> {
            room.setMinPlayers(game.getMinPlayers());
            room.setMaxPlayers(game.getMaxPlayers());
        });
        room.setStatus(GameStatus.PENDING);
        roomStore.rooms.add(room);
        return room;
    }

    @Override
    public synchronized void publishRoom(String roomId) {
        roomStore.rooms.stream()
                .filter(r -> r.getId().equals(roomId) && r.getStatus() == GameStatus.PENDING)
                .findFirst()
                .ifPresent(r -> r.setStatus(GameStatus.WAITING));
    }

    // NOTE: This GWT-RPC method is NOT protected by Spring Security.
    // Authorization is enforced here manually: only the room creator may delete via this path.
    // Admins delete rooms through the Spring Security-protected DELETE /admin/rooms/{id} endpoint instead.
    @Override
    public synchronized void deleteRoom(String roomId) {
        String callerId = getPlayerIdFromRequest();
        boolean isCreator = roomStore.rooms.stream()
                .filter(r -> r.getId().equals(roomId))
                .anyMatch(r -> r.getCreatedByUserId().equals(callerId));
        if (!isCreator) return;
        roomStore.deleteRoom(roomId);
    }

    String getPlayerIdFromRequest() {
        jakarta.servlet.http.Cookie[] cookies = getThreadLocalRequest().getCookies();
        if (cookies == null) return "";
        for (jakarta.servlet.http.Cookie c : cookies) {
            if ("playerid".equals(c.getName())) return c.getValue();
        }
        return "";
    }

    @Override
    public synchronized void updateRoom(Room room) throws RoomServiceException {
        if (room == null) {
            logger.error("Attempt to update null room");
            throw new RoomServiceException("Room cannot be null");
        }
        if (room.getId() == null || room.getId().isBlank()) {
            logger.error("Attempt to update room with null or empty ID");
            throw new RoomServiceException("Room ID cannot be null or empty");
        }
        Optional<Room> result = roomStore.rooms.stream().filter(r -> r.getId().equals(room.getId())).findFirst();
        if(result.isPresent()) {
            Room foundRoom = result.get();
            roomStore.rooms.remove(foundRoom);
            roomStore.rooms.add(room);
            logger.debug("Room {} updated successfully", room.getId());
        } else {
            logger.error("Room not found with ID: {}", room.getId());
            throw new RoomServiceException("Room with ID " + room.getId() + " not found");
        }
    }

    @Override
    public synchronized void addPlayerIdToRoom(String playerId, String roomId) {
        boolean alreadyInAnotherRoom = roomStore.rooms.stream()
                .anyMatch(r -> !r.getId().equals(roomId) && r.getPlayerIds().contains(playerId));
        if (alreadyInAnotherRoom) return;

        for (Room room1 : roomStore.rooms) {
            if (room1.getId().equals(roomId)) {
                room1.addPlayer(playerId);
                roomStore.emptyRoomTimestamps.remove(room1.getId());
                if (room1.getNrOfPlayers() >= room1.getMaxPlayers()) {
                    room1.setStatus(GameStatus.FULL);
                }
            }
        }
    }

    @Override
    public synchronized void removePlayerFromRoom(String playerId, String roomId) {
        for (Room room1 : roomStore.rooms) {
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
                    roomStore.emptyRoomTimestamps.put(room1.getId(), System.currentTimeMillis());
                }
            }
        }
    }

    @Override
    public synchronized void setUsernameAndProfile(Room room, String userId, String username, String profileId) {
        for (Room room1 : roomStore.rooms) {
            if (room1.getId().equals(room.getId())) {
                room1.addPlayerName(userId, username);
                room1.addPlayerProfile(userId, profileId);
            }
        }
    }

    @Override
    public synchronized Room startGame(String roomId) throws RoomServiceException {
        logger.debug("Starting game for room: {}", roomId);
        for (Room room1 : roomStore.rooms) {
            if (room1.getId().equals(roomId)) {

                GameDefinition game = gamesConfig.findById(room1.getGameId())
                        .orElseThrow(() -> new RoomServiceException("Unknown game: " + room1.getGameId()));

                String baseUrl = game.getBaseUrl();
                logger.debug("Game ID: {}, Base URL: {}", room1.getGameId(), baseUrl);

                // 0. Enforce minimum players
                if (room1.getPlayerNames().size() < game.getMinPlayers()) {
                    throw new RoomServiceException(
                            "Not enough players. Need at least " + game.getMinPlayers()
                            + ", but only " + room1.getPlayerNames().size() + " have joined.");
                }

                // 1. Create a game session
                Map<String, Object> newGameRequest = new HashMap<>();
                newGameRequest.put("roomName", room1.getName());
                newGameRequest.put("maxPlayers", room1.getPlayerNames().size());
                if (!room1.getGameOptions().isEmpty()) {
                    newGameRequest.put("gameOptions", parseGameOptions(room1.getGameOptions()));
                }
                Map sessionResponse;
                try {
                    String createSessionUrl = baseUrl + "/games";
                    logger.debug("Creating session at: {}", createSessionUrl);
                    sessionResponse = restTemplate.postForObject(createSessionUrl, newGameRequest, Map.class);
                    logger.debug("Session created successfully");
                } catch (RestClientException e) {
                    logger.error("Failed to create session: {}", e.getMessage(), e);
                    throw new RoomServiceException("Could not reach game server at " + baseUrl + ": " + e.getMessage());
                }
                String sessionId = sessionResponse.get("sessionId").toString();
                logger.debug("Session ID: {}", sessionId);

                // 2. Add each player & 3. Start the game — clean up the session on any failure
                try {
                    logger.debug("Adding {} players to session", room1.getPlayerNames().size());
                    for (Map.Entry<String, String> entry : room1.getPlayerNames().entrySet()) {
                        String playerId = entry.getKey();
                        String playerName = entry.getValue();
                        String profilePicStr = room1.getPlayerProfiles().get(playerId);
                        int profilePic = 0;
                        if (profilePicStr != null) {
                            try { profilePic = Integer.parseInt(profilePicStr); } catch (NumberFormatException ignored) {}
                        }
                        Map<String, Object> playerRequest = new HashMap<>();
                        playerRequest.put("id", playerId);
                        playerRequest.put("name", playerName);
                        playerRequest.put("profilePic", profilePic);
                        String playerUrl = baseUrl + "/games/" + sessionId + "/players";
                        logger.debug("Adding player {} at: {}", playerName, playerUrl);
                        restTemplate.postForObject(playerUrl, playerRequest, Map.class);
                    }

                    // 3. Start the game
                    String startGameUrl = baseUrl + "/games/" + sessionId;
                    logger.debug("Starting game at: {}", startGameUrl);
                    restTemplate.postForObject(startGameUrl, null, Void.class);
                    logger.info("Game started successfully for room: {}", roomId);
                } catch (RestClientException e) {
                    logger.error("Error during game start: {}", e.getMessage(), e);
                    // Attempt to clean up the orphaned session on the game server
                    try {
                        restTemplate.delete(baseUrl + "/games/" + sessionId);
                    } catch (RestClientException ignored) {}
                    throw new RoomServiceException(extractErrorMessage(e));
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
    public synchronized ArrayList<GameOption> getGameOptions(String gameId) throws RoomServiceException {
        GameDefinition game = gamesConfig.findById(gameId).orElse(null);
        if (game == null) return new ArrayList<>();
        try {
            GameOption[] options = restTemplate.getForObject(game.getBaseUrl() + "/game-options", GameOption[].class);
            if (options == null) return new ArrayList<>();
            return new ArrayList<>(java.util.Arrays.asList(options));
        } catch (RestClientException e) {
            return new ArrayList<>();
        }
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
        roomStore.emptyRoomTimestamps.entrySet().removeIf(entry -> {
            if (now - entry.getValue() >= EMPTY_ROOM_TTL_MS) {
                roomStore.rooms.removeIf(r -> r.getId().equals(entry.getKey()) && r.getPlayerIds().isEmpty());
                return true;
            }
            return false;
        });
    }

    @Scheduled(fixedDelay = 30_000)
    public synchronized void verifyGameSessionsExist() {
        ArrayList<Room> playingRooms = new ArrayList<>();
        for (Room room : roomStore.rooms) {
            if (room.getStatus() == GameStatus.PLAYING && room.getGameSessionId() != null) {
                playingRooms.add(room);
            }
        }

        for (Room room : playingRooms) {
            if ("Test Room".equals(room.getName())) {
                continue;
            }

            try {
                String gameUrl = room.getGameBaseUrl() + "/games/" + room.getGameSessionId();
                logger.debug("Verifying game session exists at: {}", gameUrl);
                restTemplate.getForObject(gameUrl, Map.class);
                logger.debug("Game session verified: {}", room.getId());
            } catch (RestClientException e) {
                logger.warn("Game session no longer exists for room {}: {}", room.getId(), e.getMessage());
                roomStore.rooms.remove(room);
            }
        }
    }

    private Map<String, Object> parseGameOptions(HashMap<String, String> raw) {
        Map<String, Object> parsed = new HashMap<>();
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            parsed.put(entry.getKey(), parseOptionValue(entry.getValue()));
        }
        return parsed;
    }

    private Object parseOptionValue(String value) {
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        try { return Integer.parseInt(value); } catch (NumberFormatException ignored) {}
        return value;
    }

    private String extractErrorMessage(RestClientException e) {
        if (e instanceof HttpClientErrorException hce) {
            try {
                Map<?, ?> body = new ObjectMapper().readValue(hce.getResponseBodyAsString(), Map.class);
                Object msg = body.get("message");
                if (msg != null) return msg.toString();
            } catch (Exception ignored) {}
            return hce.getStatusText();
        }
        return e.getMessage();
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