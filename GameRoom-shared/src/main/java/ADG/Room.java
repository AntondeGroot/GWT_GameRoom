package ADG;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Room implements IsSerializable {
    private String createdByUserId;
    private String name;
    private String status;
    private ArrayList<String> playerIds = new ArrayList<>();
    private HashMap<String, String> userProfiles = new HashMap<>(); // Map of playerId to profileId
    private HashMap<String, String> userNames = new HashMap<>(); // Map of playerId to userName

    public Room() {} // Default constructor

    public Room(String name, String createdByUserId) {
        setCreatedByUserId(createdByUserId);
        setName(name);
        status = "waiting for players ...";
    }

    public void addPlayer(String playerId) {
        if (!playerIds.contains(playerId)) {
            playerIds.add(playerId);
        }
    }

    public void removePlayer(String playerId) {
        playerIds.remove(playerId);
    }

    public String getName() {
        return name;
    }

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

    public void setUserProfile(String userId, String profile) {
        userProfiles.put(userId, profile);
    }

    public void setUserName(String userId, String userName) {
        userNames.put(userId, userName);
    }

    public HashMap<String, String> getUserNames() {
        return userNames;
    }

    public HashMap<String, String> getUserProfiles() {
        return userProfiles;
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
                ", userNames=" + userNames.values() +
                '}';
    }
}
