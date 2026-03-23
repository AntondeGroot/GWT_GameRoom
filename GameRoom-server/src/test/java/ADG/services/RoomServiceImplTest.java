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

    @Test
    void getRoomsHidesPendingRooms() {
        service.createRoom(buildRoom("Alpha"));
        assertTrue(service.getRooms().isEmpty(), "PENDING room must not appear in lobby list");
    }

    @Test
    void getRoomsShowsPublishedRoom() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        service.publishRoom(room.getId());
        assertEquals(1, service.getRooms().size());
        assertEquals(GameStatus.WAITING, service.getRooms().get(0).getStatus());
    }

    // ── createRoom ───────────────────────────────────────────────────────────

    @Test
    void createRoomAddsAndReturnsRoom() {
        Room room = buildRoom("Alpha");
        Room result = service.createRoom(room);

        assertSame(room, result);
        assertEquals(GameStatus.PENDING, result.getStatus(), "newly created room must be PENDING");
        assertTrue(service.getRooms().isEmpty(), "PENDING room must not be visible in lobby");
    }

    // ── publishRoom ──────────────────────────────────────────────────────────

    @Test
    void publishRoomMakesRoomVisible() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        service.publishRoom(room.getId());
        assertEquals(1, service.getRooms().size());
    }

    @Test
    void publishRoomSetsStatusToWaiting() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        service.publishRoom(room.getId());
        assertEquals(GameStatus.WAITING, service.getRooms().get(0).getStatus());
    }

    @Test
    void publishRoomOnNonPendingRoomIsNoOp() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        service.publishRoom(room.getId());
        // calling again should not break anything
        service.publishRoom(room.getId());
        assertEquals(GameStatus.WAITING, service.getRooms().get(0).getStatus());
    }

    @Test
    void publishRoomOnUnknownIdIsNoOp() {
        service.publishRoom("non-existent-id"); // must not throw
        assertTrue(service.getRooms().isEmpty());
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
        assertNotNull(service.getRoomById(room.getId()), "original room must still exist");
    }

    @Test
    void createRoomDuplicateNameDifferentObjectThrows() {
        // Same name but different object (different players/status) — the real duplicate-name bug
        Room original = buildRoom("Alpha");
        service.createRoom(original);
        Room other = buildRoom("Alpha");
        other.addPlayer("some-player");
        assertThrows(IllegalArgumentException.class, () -> service.createRoom(other));
        assertNotNull(service.getRoomById(original.getId()), "original room must still exist");
    }

    @Test
    void createRoomDuplicateNameCaseInsensitiveThrows() {
        Room original = buildRoom("Alpha");
        service.createRoom(original);
        assertThrows(IllegalArgumentException.class, () -> service.createRoom(buildRoom("alpha")));
        assertThrows(IllegalArgumentException.class, () -> service.createRoom(buildRoom("ALPHA")));
        assertNotNull(service.getRoomById(original.getId()), "original room must still exist");
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
    void createRoomSetsMaxPlayersFromConfig() {
        GameDefinition game = mock(GameDefinition.class);
        when(game.getMaxPlayers()).thenReturn(6);
        when(gamesConfig.findById("keezen")).thenReturn(Optional.of(game));

        Room room = buildRoom("Alpha");
        service.createRoom(room);

        assertEquals(6, room.getMaxPlayers());
    }

    @Test
    void createRoomLeavesMinPlayersAtDefaultWhenGameNotFound() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        assertEquals(1, room.getMinPlayers()); // Room default
    }

    @Test
    void createRoomLeavesMaxPlayersAtDefaultWhenGameNotFound() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        assertEquals(8, room.getMaxPlayers()); // Room default
    }

    @Test
    void createRoomRemovesCreatorFromPreviousRoom() {
        Room roomA = buildRoom("Alpha");
        service.createRoom(roomA);
        service.publishRoom(roomA.getId());
        service.addPlayerIdToRoom("creator-1", roomA);

        Room roomB = buildRoom("Beta"); // same createdByUserId ("creator-1")
        service.createRoom(roomB);
        service.publishRoom(roomB.getId());

        assertEquals(0, service.getRoomById(roomA.getId()).getNrOfPlayers());
        assertEquals(2, service.getRooms().size());
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
    void addPlayerIdToRoomSetsStatusToFullWhenAtCapacity() {
        Room room = buildRoom("Alpha");
        room.setMaxPlayers(2);
        service.createRoom(room);
        service.publishRoom(room.getId());
        service.addPlayerIdToRoom("player-1", room);

        assertEquals(GameStatus.WAITING, service.getRoomById(room.getId()).getStatus());

        service.addPlayerIdToRoom("player-2", room);

        assertEquals(GameStatus.FULL, service.getRoomById(room.getId()).getStatus());
    }

    @Test
    void addPlayerIdToRoomStatusRemainsWaitingBelowCapacity() {
        Room room = buildRoom("Alpha");
        room.setMaxPlayers(4);
        service.createRoom(room);
        service.publishRoom(room.getId());
        service.addPlayerIdToRoom("player-1", room);
        service.addPlayerIdToRoom("player-2", room);
        service.addPlayerIdToRoom("player-3", room);

        assertEquals(GameStatus.WAITING, service.getRoomById(room.getId()).getStatus());
    }

    @Test
    void addPlayerIdToRoomIgnoresPlayerAlreadyInAnotherRoom() {
        Room roomA = buildRoom("Alpha");
        Room roomB = buildRoom("Beta");
        service.createRoom(roomA);
        service.createRoom(roomB);
        service.addPlayerIdToRoom("player-1", roomA);

        service.addPlayerIdToRoom("player-1", roomB); // should be silently rejected

        assertEquals(1, service.getRoomById(roomA.getId()).getNrOfPlayers());
        assertEquals(0, service.getRoomById(roomB.getId()).getNrOfPlayers());
    }

    @Test
    void addPlayerIdToRoomAllowsRejoiningSameRoom() {
        Room room = buildRoom("Alpha");
        service.createRoom(room);
        service.addPlayerIdToRoom("player-1", room);
        service.addPlayerIdToRoom("player-1", room); // rejoin = idempotent

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
    void removePlayerFromFullRoomRevertsStatusToWaiting() {
        Room room = buildRoom("Alpha");
        room.setMaxPlayers(2);
        service.createRoom(room);
        service.addPlayerIdToRoom("player-1", room);
        service.addPlayerIdToRoom("player-2", room);
        assertEquals(GameStatus.FULL, service.getRoomById(room.getId()).getStatus());

        service.removePlayerFromRoom("player-1", room);

        assertEquals(GameStatus.WAITING, service.getRoomById(room.getId()).getStatus());
    }

    @Test
    void removePlayerFromWaitingRoomKeepsStatusWaiting() {
        Room room = buildRoom("Alpha");
        room.setMaxPlayers(4);
        service.createRoom(room);
        service.publishRoom(room.getId());
        service.addPlayerIdToRoom("player-1", room);
        service.addPlayerIdToRoom("player-2", room);

        service.removePlayerFromRoom("player-1", room);

        assertEquals(GameStatus.WAITING, service.getRoomById(room.getId()).getStatus());
    }

    @Test
    void removePlayerFromPlayingRoomKeepsStatusPlaying() {
        Room room = buildRoom("Alpha");
        room.setMaxPlayers(2);
        service.createRoom(room);
        service.addPlayerIdToRoom("player-1", room);
        service.addPlayerIdToRoom("player-2", room);
        room.setStatus(GameStatus.PLAYING); // game started

        service.removePlayerFromRoom("player-1", room);

        assertEquals(GameStatus.PLAYING, service.getRoomById(room.getId()).getStatus());
    }

    @Test
    void playerCanJoinNewRoomAfterLeavingPreviousOne() {
        Room roomA = buildRoom("Alpha");
        Room roomB = buildRoom("Beta");
        service.createRoom(roomA);
        service.createRoom(roomB);
        service.addPlayerIdToRoom("player-1", roomA);

        service.removePlayerFromRoom("player-1", roomA);
        service.addPlayerIdToRoom("player-1", roomB);

        assertEquals(0, service.getRoomById(roomA.getId()).getNrOfPlayers());
        assertEquals(1, service.getRoomById(roomB.getId()).getNrOfPlayers());
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
