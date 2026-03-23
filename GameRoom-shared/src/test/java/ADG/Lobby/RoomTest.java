package ADG.Lobby;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    private Room room;

    @BeforeEach
    void setUp() {
        // Room(name, userId) is the parameterised constructor used everywhere on the client.
        // On the server we use the no-arg constructor + setters, so we test both.
        room = new Room();
        room.setId("room-1");
        room.setName("Test Room");
        room.setCreatedByUserId("creator-1");
        room.setStatus(GameStatus.WAITING);
    }

    // ── construction ────────────────────────────────────────────────────────

    @Test
    void noArgConstructorProducesEmptyRoom() {
        Room r = new Room();
        assertNull(r.getId());
        assertNull(r.getName());
        assertNull(r.getCreatedByUserId());
        assertTrue(r.getPlayerIds().isEmpty());
        assertTrue(r.getPlayerNames().isEmpty());
        assertTrue(r.getPlayerProfiles().isEmpty());
    }

    @Test
    void settersRoundTrip() {
        assertEquals("room-1", room.getId());
        assertEquals("Test Room", room.getName());
        assertEquals("creator-1", room.getCreatedByUserId());
        assertEquals(GameStatus.WAITING, room.getStatus());
    }

    // ── player management ────────────────────────────────────────────────────

    @Test
    void addPlayerAppendsId() {
        room.addPlayer("p1");
        assertEquals(1, room.getNrOfPlayers());
        assertTrue(room.getPlayerIds().contains("p1"));
    }

    @Test
    void addPlayerIgnoresDuplicates() {
        room.addPlayer("p1");
        room.addPlayer("p1");
        assertEquals(1, room.getNrOfPlayers());
    }

    @Test
    void addPlayerNameAndProfileStoredCorrectly() {
        room.addPlayer("p1");
        room.addPlayerName("p1", "Alice");
        room.addPlayerProfile("p1", "3");

        assertEquals("Alice", room.getPlayerNames().get("p1"));
        assertEquals("3", room.getPlayerProfiles().get("p1"));
    }

    @Test
    void removePlayerClearsAllMaps() {
        room.addPlayer("p1");
        room.addPlayerName("p1", "Alice");
        room.addPlayerProfile("p1", "3");

        room.removePlayer("p1");

        assertFalse(room.getPlayerIds().contains("p1"));
        assertFalse(room.getPlayerNames().containsKey("p1"));
        assertFalse(room.getPlayerProfiles().containsKey("p1"));
    }

    @Test
    void removeNonExistentPlayerIsNoOp() {
        room.addPlayer("p1");
        room.removePlayer("nobody");
        assertEquals(1, room.getNrOfPlayers());
    }

    @Test
    void getNrOfPlayersReflectsCurrentState() {
        assertEquals(0, room.getNrOfPlayers());
        room.addPlayer("p1");
        room.addPlayer("p2");
        assertEquals(2, room.getNrOfPlayers());
        room.removePlayer("p1");
        assertEquals(1, room.getNrOfPlayers());
    }

    // ── options ──────────────────────────────────────────────────────────────

    @Test
    void uniqueProfilePicsDefaultsTrue() {
        assertTrue(new Room().isUniqueProfilePics());
    }

    @Test
    void setUniqueProfilePicsRoundTrips() {
        room.setUniqueProfilePics(true);
        assertTrue(room.isUniqueProfilePics());
        room.setUniqueProfilePics(false);
        assertFalse(room.isUniqueProfilePics());
    }

    @Test
    void minPlayersDefaultsToOne() {
        assertEquals(1, new Room().getMinPlayers());
    }

    @Test
    void setMinPlayersRoundTrips() {
        room.setMinPlayers(4);
        assertEquals(4, room.getMinPlayers());
    }

    // ── equality ─────────────────────────────────────────────────────────────

    private Room buildIdenticalRoom() {
        Room other = new Room();
        other.setId("room-1");
        other.setName("Test Room");
        other.setCreatedByUserId("creator-1");
        other.setStatus(GameStatus.WAITING);
        return other;
    }

    @Test
    void identicalRoomsAreEqual() {
        assertEquals(room, buildIdenticalRoom());
    }

    @Test
    void equalRoomsHaveSameHashCode() {
        assertEquals(room.hashCode(), buildIdenticalRoom().hashCode());
    }

    @Test
    void roomsDifferById() {
        Room other = buildIdenticalRoom();
        other.setId("other-id");
        assertNotEquals(room, other);
    }

    @Test
    void roomsDifferByName() {
        Room other = buildIdenticalRoom();
        other.setName("Other Room");
        assertNotEquals(room, other);
    }

    @Test
    void roomsDifferByStatus() {
        Room other = buildIdenticalRoom();
        other.setStatus(GameStatus.PLAYING);
        assertNotEquals(room, other);
    }

    @Test
    void roomsDifferByPlayerIds() {
        Room other = buildIdenticalRoom();
        other.addPlayer("p1");
        assertNotEquals(room, other);
    }

    @Test
    void roomsDifferByGameId() {
        Room other = buildIdenticalRoom();
        other.setGameId("other-game");
        assertNotEquals(room, other);
    }

    @Test
    void roomsDifferByMinPlayers() {
        Room other = buildIdenticalRoom();
        other.setMinPlayers(4);
        assertNotEquals(room, other);
    }

    @Test
    void roomsDifferByMaxPlayers() {
        Room other = buildIdenticalRoom();
        other.setMaxPlayers(4);
        assertNotEquals(room, other);
    }

    @Test
    void roomsDifferByPlayerNames() {
        Room other = buildIdenticalRoom();
        other.addPlayer("p1");
        other.addPlayerName("p1", "Alice");
        assertNotEquals(room, other);
    }

    @Test
    void roomsDifferByCreatedByUserId() {
        Room other = buildIdenticalRoom();
        other.setCreatedByUserId("other-creator");
        assertNotEquals(room, other);
    }
}
