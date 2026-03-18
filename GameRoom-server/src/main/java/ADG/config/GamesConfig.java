package ADG.config;

import ADG.Lobby.GameDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "games")
public class GamesConfig {

    private List<GameDefinition> available = new ArrayList<>();

    public List<GameDefinition> getAvailable() { return available; }
    public void setAvailable(List<GameDefinition> available) { this.available = available; }

    public Optional<GameDefinition> findById(String id) {
        return available.stream().filter(g -> g.getId().equals(id)).findFirst();
    }
}