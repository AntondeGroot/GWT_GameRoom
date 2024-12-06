package ADG;

import java.io.Serializable;

public class RoomInfo implements Serializable  {
    private Long id;
    private String name;
    private Integer nrOfPlayers;

    public RoomInfo() {} // Default constructor

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNrOfPlayers() {
        return nrOfPlayers;
    }

    public void setNrOfPlayers(Integer nrOfPlayers) {
        this.nrOfPlayers = nrOfPlayers;
    }
}
