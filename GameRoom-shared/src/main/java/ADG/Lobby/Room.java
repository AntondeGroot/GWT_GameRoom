package ADG.Lobby;

import ADG.UUID;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Room implements IsSerializable {
    private String id;
    private String createdByUserId;
    private String name;
    private GameStatus status;
    private ArrayList<String> playerIds = new ArrayList<>();
    private HashMap<String, String> playerProfiles = new HashMap<>(); // Map of playerId to profileId
    private HashMap<String, String> playerNames = new HashMap<>(); // Map of playerId to userName

    public Room() {} // Default constructor

    public Room(String name, String createdByUserId) {
        setCreatedByUserId(createdByUserId);
        setName(name);
        setId(UUID.get());
        status = GameStatus.WAITING;
    }

    public void addPlayer(String playerId) {
        if (!playerIds.contains(playerId)) {
            playerIds.add(playerId);
        }
    }

    public void addPlayerName(String playerId, String name) {
        playerNames.put(playerId, name);
    }

    public void addPlayerProfile(String playerId, String profile) {
        playerProfiles.put(playerId, profile);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void removePlayer(String playerId) {
        playerIds.remove(playerId);
        playerNames.remove(playerId);
        playerProfiles.remove(playerId);
    }

    public String getName() {
        return name;
    }

    public GameStatus getStatus() {return status;}

    public void setStatus(GameStatus status) {this.status = status;}

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNrOfPlayers() {
        return playerIds.size();
    }

    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public HashMap<String, String> getPlayerNames() {
        return playerNames;
    }

    public HashMap<String, String> getPlayerProfiles() {
        return playerProfiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(name, room.name) && Objects.equals(status, room.status) && Objects.equals(playerIds, room.playerIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, status, playerIds);
    }

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", playerIds=" + playerIds +
                ", userNames=" + playerNames +
                '}';
    }
}
