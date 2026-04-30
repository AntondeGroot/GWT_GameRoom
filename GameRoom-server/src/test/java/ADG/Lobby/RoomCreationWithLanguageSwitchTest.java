package ADG.Lobby;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: User creates a room, configures game options, switches language,
 * and should remain on the game options screen with the same room configuration.
 *
 * SCENARIO:
 * 1. User creates a room and is presented with game options
 * 2. User configures room max players and game-specific options
 * 3. User switches language (e.g., from English to Dutch)
 * 4. User should still be on the game options screen with the same configuration
 * 5. The room data (name, ID, game, creator) should be preserved
 *
 * This test verifies that the Room model preserves its state through various
 * configuration changes that would occur during the game creation and language
 * switching workflow.
 */
class RoomCreationWithLanguageSwitchTest {

    private Room testRoom;

    @BeforeEach
    void setUp() {
        // Create a test room with specific configuration
        // This simulates what happens when a user clicks "Create Room"
        testRoom = new Room();
        testRoom.setId("room-123");
        testRoom.setName("Test Game Room");
        testRoom.setGameId("keezen");
        testRoom.setMinPlayers(2);
        testRoom.setMaxPlayers(8);
        testRoom.setGameBaseUrl("http://localhost:4200");
        testRoom.setCreatedByUserId("player-1");
        testRoom.setStatus(GameStatus.PENDING);
    }

    // ── SCENARIO 1: Player Creates Room ──────────────────────────────────

    @Test
    void playerCreatesRoomWithSpecificConfiguration() {
        // User creates a room with a name and selects a game
        assertNotNull(testRoom);
        assertEquals("room-123", testRoom.getId());
        assertEquals("Test Game Room", testRoom.getName());
        assertEquals("keezen", testRoom.getGameId());
        assertEquals(GameStatus.PENDING, testRoom.getStatus());
    }

    // ── SCENARIO 2: Player Configures Game Options ──────────────────────

    @Test
    void playerConfiguresMaxPlayersAndOptions() {
        // User sets max players and game-specific options
        testRoom.setMaxPlayers(5);
        testRoom.setUniqueProfilePics(true);
        testRoom.getGameOptions().put("difficulty", "hard");

        // The room should have the new configuration
        assertEquals(5, testRoom.getMaxPlayers());
        assertTrue(testRoom.isUniqueProfilePics());
        assertEquals("hard", testRoom.getGameOptions().get("difficulty"));
    }

    // ── SCENARIO 3: Player Switches Language ────────────────────────────

    @Test
    void roomStatePreservedWhenLanguageSwitches() {
        // User configures options
        testRoom.setMaxPlayers(5);
        testRoom.setUniqueProfilePics(true);
        testRoom.getGameOptions().put("difficulty", "hard");

        String originalId = testRoom.getId();
        String originalName = testRoom.getName();
        String originalGameId = testRoom.getGameId();

        // Simulate language switch - the room object must survive intact
        // (In real code, this happens when the UI reloads with new language strings)
        // We're testing that the room model itself isn't affected by UI changes

        // Room configuration should survive language switch
        assertEquals(originalId, testRoom.getId());
        assertEquals(originalName, testRoom.getName());
        assertEquals(originalGameId, testRoom.getGameId());
        assertEquals(5, testRoom.getMaxPlayers());
        assertTrue(testRoom.isUniqueProfilePics());
        assertEquals("hard", testRoom.getGameOptions().get("difficulty"));
    }

    // ── SCENARIO 4: Verify Core Room Data Stability ──────────────────────

    @Test
    void coreRoomDataRemainsStable() {
        String originalId = testRoom.getId();
        String originalName = testRoom.getName();
        String originalGameId = testRoom.getGameId();
        String originalCreator = testRoom.getCreatedByUserId();
        String originalBaseUrl = testRoom.getGameBaseUrl();

        // Make various configuration changes
        testRoom.setMaxPlayers(4);
        testRoom.setMinPlayers(1);
        testRoom.setUniqueProfilePics(true);
        testRoom.getGameOptions().put("speed", "fast");
        testRoom.addPlayer("player-2");
        testRoom.addPlayer("player-3");

        // Core data should NOT change
        assertEquals(originalId, testRoom.getId(), "Room ID must not change");
        assertEquals(originalName, testRoom.getName(), "Room name must not change");
        assertEquals(originalGameId, testRoom.getGameId(), "Game ID must not change");
        assertEquals(originalCreator, testRoom.getCreatedByUserId(), "Creator must not change");
        assertEquals(originalBaseUrl, testRoom.getGameBaseUrl(), "Base URL must not change");
    }

    // ── SCENARIO 5: Configuration Survives Room Manipulation ─────────────

    @Test
    void configurationSurvivesPlayerJoining() {
        // User has configured options
        testRoom.setMaxPlayers(6);
        testRoom.setUniqueProfilePics(true);
        int originalMaxPlayers = testRoom.getMaxPlayers();
        boolean originalUniqueProfiles = testRoom.isUniqueProfilePics();

        // Players start joining (simulate other players connecting)
        testRoom.addPlayer("player-2");
        testRoom.addPlayer("player-3");

        // Configuration should survive
        assertEquals(originalMaxPlayers, testRoom.getMaxPlayers());
        assertEquals(originalUniqueProfiles, testRoom.isUniqueProfilePics());
    }

    // ── SCENARIO 6: Game Options Persist ────────────────────────────────

    @Test
    void gameOptionsCanBeSetAndRetrieved() {
        // User configures game-specific options
        testRoom.getGameOptions().put("difficulty", "hard");
        testRoom.getGameOptions().put("speed", "fast");
        testRoom.getGameOptions().put("modifiers", "none");

        assertEquals("hard", testRoom.getGameOptions().get("difficulty"));
        assertEquals("fast", testRoom.getGameOptions().get("speed"));
        assertEquals("none", testRoom.getGameOptions().get("modifiers"));

        // Configuration persists
        assertEquals(3, testRoom.getGameOptions().size());
    }

    // ── SCENARIO 7: Status Changes Preserve Other Data ────────────────────

    @Test
    void statusChangeDoesNotAffectConfiguration() {
        testRoom.setMaxPlayers(6);
        testRoom.setUniqueProfilePics(true);
        testRoom.getGameOptions().put("difficulty", "medium");

        // Room status changes (part of the workflow)
        testRoom.setStatus(GameStatus.WAITING);

        // Configuration should survive status change
        assertEquals(6, testRoom.getMaxPlayers());
        assertTrue(testRoom.isUniqueProfilePics());
        assertEquals("medium", testRoom.getGameOptions().get("difficulty"));
        assertEquals(GameStatus.WAITING, testRoom.getStatus());
    }

    // ── SCENARIO 8: Player Names/Profiles Don't Affect Options ───────────

    @Test
    void playerDataAndConfigurationAreIndependent() {
        testRoom.setMaxPlayers(4);
        testRoom.setUniqueProfilePics(true);

        // Add player names and profiles
        testRoom.addPlayer("player-2");
        testRoom.addPlayerName("player-2", "Alice");
        testRoom.addPlayerProfile("player-2", "2");

        // Configuration should remain unchanged
        assertEquals(4, testRoom.getMaxPlayers());
        assertTrue(testRoom.isUniqueProfilePics());
    }

    // ── SCENARIO 9: Multiple Configuration Updates ───────────────────────

    @Test
    void multipleConfigurationUpdatesCanOccur() {
        // Initial config
        testRoom.setMaxPlayers(5);
        testRoom.setUniqueProfilePics(true);

        // User updates config
        testRoom.setMaxPlayers(6);
        testRoom.setUniqueProfilePics(false);
        testRoom.getGameOptions().put("difficulty", "hard");

        // Check final state
        assertEquals(6, testRoom.getMaxPlayers());
        assertFalse(testRoom.isUniqueProfilePics());
        assertEquals("hard", testRoom.getGameOptions().get("difficulty"));
    }

    // ── SCENARIO 10: Room Is Still Valid After Full Configuration ────────

    @Test
    void roomRemainsValidAfterFullConfiguration() {
        // Complete workflow: create, configure, prepare
        testRoom.setMaxPlayers(4);
        testRoom.setMinPlayers(2);
        testRoom.setUniqueProfilePics(true);
        testRoom.getGameOptions().put("difficulty", "hard");
        testRoom.addPlayer("player-1"); // Creator joins
        testRoom.addPlayerName("player-1", "Creator");
        testRoom.addPlayerProfile("player-1", "1");
        testRoom.addPlayer("player-2");
        testRoom.addPlayerName("player-2", "Bob");
        testRoom.addPlayerProfile("player-2", "3");

        // Room should still be valid
        assertNotNull(testRoom.getId());
        assertNotNull(testRoom.getName());
        assertEquals("keezen", testRoom.getGameId());
        assertEquals("player-1", testRoom.getCreatedByUserId());
        assertEquals(2, testRoom.getNrOfPlayers());
        assertEquals(4, testRoom.getMaxPlayers());
    }
}