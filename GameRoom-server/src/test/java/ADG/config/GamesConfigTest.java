package ADG.config;

import ADG.Lobby.GameDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GamesConfigTest {

    private GamesConfig config;

    @BeforeEach
    void setUp() {
        config = new GamesConfig();
    }

    private GameDefinition buildGameDef(String id) {
        GameDefinition g = new GameDefinition();
        g.setId(id);
        g.setName("Game: " + id);
        g.setBaseUrl("http://localhost:" + (4200 + id.length()));
        g.setHealthUrl("http://localhost:" + (4200 + id.length()) + "/health");
        g.setMinPlayers(2);
        g.setMaxPlayers(8);
        return g;
    }

    // ── getAvailable ─────────────────────────────────────────────────────

    @Test
    void getAvailableReturnsEmptyListByDefault() {
        List<GameDefinition> games = config.getAvailable();
        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    void getAvailableReturnsConfiguredGames() {
        List<GameDefinition> games = new ArrayList<>();
        games.add(buildGameDef("keezen"));
        games.add(buildGameDef("qwixx"));
        config.setAvailable(games);

        List<GameDefinition> result = config.getAvailable();
        assertEquals(2, result.size());
    }

    // ── setAvailable ─────────────────────────────────────────────────────

    @Test
    void setAvailableUpdatesGames() {
        List<GameDefinition> games = new ArrayList<>();
        games.add(buildGameDef("game1"));
        config.setAvailable(games);

        assertEquals(1, config.getAvailable().size());
        assertEquals("game1", config.getAvailable().get(0).getId());
    }

    @Test
    void setAvailableReplacesExistingGames() {
        List<GameDefinition> games1 = new ArrayList<>();
        games1.add(buildGameDef("game1"));
        config.setAvailable(games1);

        List<GameDefinition> games2 = new ArrayList<>();
        games2.add(buildGameDef("game2"));
        games2.add(buildGameDef("game3"));
        config.setAvailable(games2);

        assertEquals(2, config.getAvailable().size());
        assertFalse(config.getAvailable().stream().anyMatch(g -> g.getId().equals("game1")));
    }

    // ── findById ─────────────────────────────────────────────────────────

    @Test
    void findByIdReturnsGameWhenFound() {
        List<GameDefinition> games = new ArrayList<>();
        games.add(buildGameDef("keezen"));
        games.add(buildGameDef("qwixx"));
        config.setAvailable(games);

        Optional<GameDefinition> result = config.findById("keezen");

        assertTrue(result.isPresent());
        assertEquals("keezen", result.get().getId());
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        List<GameDefinition> games = new ArrayList<>();
        games.add(buildGameDef("keezen"));
        config.setAvailable(games);

        Optional<GameDefinition> result = config.findById("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void findByIdReturnsEmptyWhenListEmpty() {
        config.setAvailable(new ArrayList<>());

        Optional<GameDefinition> result = config.findById("anything");

        assertFalse(result.isPresent());
    }

    @Test
    void findByIdIsCaseSensitive() {
        List<GameDefinition> games = new ArrayList<>();
        games.add(buildGameDef("Keezen"));
        config.setAvailable(games);

        Optional<GameDefinition> result = config.findById("keezen");

        assertFalse(result.isPresent());
    }

    @Test
    void findByIdWithNullReturnsEmpty() {
        List<GameDefinition> games = new ArrayList<>();
        games.add(buildGameDef("keezen"));
        config.setAvailable(games);

        Optional<GameDefinition> result = config.findById(null);

        assertFalse(result.isPresent());
    }

    @Test
    void findByIdReturnsFirsMatchWhenDuplicates() {
        List<GameDefinition> games = new ArrayList<>();
        GameDefinition g1 = buildGameDef("keezen");
        GameDefinition g2 = buildGameDef("keezen");
        g2.setName("Duplicate");
        games.add(g1);
        games.add(g2);
        config.setAvailable(games);

        Optional<GameDefinition> result = config.findById("keezen");

        assertTrue(result.isPresent());
        assertEquals("Game: keezen", result.get().getName());
    }

    @Test
    void findByIdReturnsCorrectGameProperties() {
        List<GameDefinition> games = new ArrayList<>();
        GameDefinition g = buildGameDef("testgame");
        g.setMinPlayers(4);
        g.setMaxPlayers(6);
        games.add(g);
        config.setAvailable(games);

        Optional<GameDefinition> result = config.findById("testgame");

        assertTrue(result.isPresent());
        assertEquals(4, result.get().getMinPlayers());
        assertEquals(6, result.get().getMaxPlayers());
    }

    @Test
    void availableGamesPreserveInsertionOrder() {
        List<GameDefinition> games = new ArrayList<>();
        games.add(buildGameDef("alpha"));
        games.add(buildGameDef("beta"));
        games.add(buildGameDef("gamma"));
        config.setAvailable(games);

        List<GameDefinition> result = config.getAvailable();
        assertEquals("alpha", result.get(0).getId());
        assertEquals("beta", result.get(1).getId());
        assertEquals("gamma", result.get(2).getId());
    }
}