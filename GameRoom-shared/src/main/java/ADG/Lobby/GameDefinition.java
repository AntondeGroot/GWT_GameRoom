package ADG.Lobby;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GameDefinition implements IsSerializable {

    private String id;
    private String name;
    private String baseUrl;
    private String healthUrl;
    private int minPlayers;
    private int maxPlayers;

    public GameDefinition() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getHealthUrl() { return healthUrl != null ? healthUrl : baseUrl; }
    public void setHealthUrl(String healthUrl) { this.healthUrl = healthUrl; }

    public int getMinPlayers() { return minPlayers; }
    public void setMinPlayers(int minPlayers) { this.minPlayers = minPlayers; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
}