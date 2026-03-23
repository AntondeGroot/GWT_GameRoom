package ADG.Lobby;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum GameStatus implements IsSerializable {
    PENDING,
    WAITING,
    FULL,
    PLAYING
}
