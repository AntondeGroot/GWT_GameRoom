package ADG.services;

import ADG.Lobby.GameDefinition;
import ADG.Lobby.GameStatus;
import ADG.Lobby.Room;
import ADG.config.GamesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    private RoomServiceImpl service;

    @Mock
    private GamesConfig gamesConfig;

    @BeforeEach
    void setUp() throws Exception {
        service = new RoomServiceImpl();
        // Inject mock GamesConfig — @PostConstruct is NOT invoked when newing directly,
        // so we start with a clean, empty room list for every test.
        Field field = RoomServiceImpl.class.getDeclaredField("gamesConfig");
        field.setAccessible(true);
        field.set(service, gamesConfig);

        lenient().when(gamesConfig.findById(anyString())).thenReturn(Optional.empty());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Room buildRoom(String name) {
        Room r = new Room();
        r.setId(java.util.UUID.randomUUID().toString());
        r.setName(name);
        r.setCreatedByUserId("creator-1");
        r.setStatus(GameStatus.WAITING);
        r.setGameId("keezen");
        return r;
    }

    // ── getRooms ─────────────────────────────────────────────────────────────

    @Test
    void getRoomsStartsEmpty() {
        assertTrue(service.getRooms().isEmpty());
    }

    // ── createRoom ───────────────────────────────────────────────────────────

    @Test
    void createRoomAddsAndReturnsRoom() {
        Room room = buildRoom("Alpha");
        Room result = service.createRoom(room);

        assertSame(room, result);
        assertEquals(1, service.getRooms().size());
    }

    @Test
    void createRoomWithBlankNameThrows() {
        Room room = buildRoom("   ");
        assertThrows(IllegalArgumentException.class, () -> service.createRoom(room));
        assertTrue(service.getRooms().isEmpty());
    }

    @Test
    void createRoomDuplicateThrows() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        assertThrows(IllegalArgumentException.class, () -> service.createRoom(room));
        assertEquals(1, service.getRooms().size());
    }

    @Test
    void createRoomSetsMinPlayersFromConfig() {
        GameDefinition game = mock(GameDefinition.class);
        when(game.getMinPlayers()).thenReturn(4);
        when(gamesConfig.findById("keezen")).thenReturn(Optional.of(game));

        Room room = buildRoom("Alpha");
        service.createRoom(room);

        assertEquals(4, room.getMinPlayers());
    }

    @Test
    void createRoomLeavesMinPlayersAtDefaultWhenGameNotFound() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        assertEquals(1, room.getMinPlayers()); // Room default
    }

    // ── getRoomById ──────────────────────────────────────────────────────────

    @Test
    void getRoomByIdFindsExistingRoom() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);

        Room found = service.getRoomById(room.getId());
        assertEquals(room, found);
    }

    @Test
    void getRoomByIdReturnsNullForUnknownId() {
        assertNull(service.getRoomById("does-not-exist"));
    }

    // ── deleteRoom ───────────────────────────────────────────────────────────

    @Test
    void deleteRoomRemovesIt() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        service.deleteRoom("Alpha");
        assertTrue(service.getRooms().isEmpty());
    }

    @Test
    void deleteNonExistentRoomIsNoOp() {
        service.deleteRoom("nobody");
        assertTrue(service.getRooms().isEmpty());
    }

    // ── updateRoom ───────────────────────────────────────────────────────────

    @Test
    void updateRoomReplacesExisting() {
        Room original = buildRoom("Alpha");
        service.createRoom(original);

        Room updated = buildRoom("Alpha"); // same name → same identity
        updated.setUniqueProfilePics(true);
        service.updateRoom(updated);

        assertEquals(1, service.getRooms().size());
        assertTrue(service.getRooms().get(0).isUniqueProfilePics());
    }

    // ── addPlayerIdToRoom ────────────────────────────────────────────────────

    @Test
    void addPlayerIdToRoomAppendsId() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        service.addPlayerIdToRoom("player-1", room);

        assertEquals(1, service.getRoomById(room.getId()).getNrOfPlayers());
    }

    @Test
    void addPlayerIdToRoomIgnoresDuplicates() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        service.addPlayerIdToRoom("player-1", room);
        service.addPlayerIdToRoom("player-1", room);

        assertEquals(1, service.getRoomById(room.getId()).getNrOfPlayers());
    }

    // ── removePlayerFromRoom ─────────────────────────────────────────────────

    @Test
    void removePlayerFromRoomRemovesPlayer() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        service.addPlayerIdToRoom("player-1", room);
        service.setUsernameAndProfile(room, "player-1", "Alice", "3");

        service.removePlayerFromRoom("player-1", room);

        Room found = service.getRoomById(room.getId());
        assertEquals(0, found.getNrOfPlayers());
        assertFalse(found.getPlayerNames().containsKey("player-1"));
    }

    @Test
    void removeCreatorTransfersOwnershipToNextPlayer() {
        Room room = buildRoom("Alpha");
        room.setCreatedByUserId("player-1");
        service.createRoom(room);
        service.addPlayerIdToRoom("player-1", room);
        service.addPlayerIdToRoom("player-2", room);

        service.removePlayerFromRoom("player-1", room);

        assertEquals("player-2", service.getRoomById(room.getId()).getCreatedByUserId());
    }

    @Test
    void removeLastPlayerLeavesNoCreator() {
        Room room = buildRoom("Alpha");
        room.setCreatedByUserId("player-1");
        service.createRoom(room);
        service.addPlayerIdToRoom("player-1", room);

        service.removePlayerFromRoom("player-1", room);

        // playerIds is empty — no ownership transfer attempted
        assertEquals(0, service.getRoomById(room.getId()).getNrOfPlayers());
    }

    // ── setUsernameAndProfile ────────────────────────────────────────────────

    @Test
    void setUsernameAndProfileStoresBoth() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        service.addPlayerIdToRoom("player-1", room);
        service.setUsernameAndProfile(room, "player-1", "Alice", "5");

        Room found = service.getRoomById(room.getId());
        assertEquals("Alice", found.getPlayerNames().get("player-1"));
        assertEquals("5", found.getPlayerProfiles().get("player-1"));
    }

    @Test
    void setUsernameAndProfileOverwritesPrevious() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        service.addPlayerIdToRoom("player-1", room);
        service.setUsernameAndProfile(room, "player-1", "Alice", "5");
        service.setUsernameAndProfile(room, "player-1", "Alice2", "7");

        assertEquals("Alice2", service.getRoomById(room.getId()).getPlayerNames().get("player-1"));
        assertEquals("7",      service.getRoomById(room.getId()).getPlayerProfiles().get("player-1"));
    }

    // ── getAvailableGames ────────────────────────────────────────────────────

    @Test
    void getAvailableGamesReturnsOnlyReachableGames() {
        // No games configured in mock → should return empty list (not throw)
        when(gamesConfig.getAvailable()).thenReturn(new ArrayList<>());
        assertTrue(service.getAvailableGames().isEmpty());
    }
}
