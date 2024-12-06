package ADG;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class RoomResponse implements IsSerializable {
    private ArrayList<String> roomNames;
    private ArrayList<Integer> nrOfPlayers;

    public RoomResponse() {}

    public ArrayList<String> getRoomNames() {
        return roomNames;
    }

    public void setRoomNames(ArrayList<String> roomNames) {
        this.roomNames = roomNames;
    }

    public ArrayList<Integer> getNrOfPlayers() {
        return nrOfPlayers;
    }

    public void setNrOfPlayers(ArrayList<Integer> nrOfPlayers) {
        this.nrOfPlayers = nrOfPlayers;
    }
}

