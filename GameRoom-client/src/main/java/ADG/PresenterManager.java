package ADG;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class PresenterManager {
    // Views
    private final GameLobbyView lobbyView = new GameLobbyView();
    private final GameRoomView roomView = new GameRoomView();
    // Presenters
    private GameLobbyPresenter lobbyPresenter;
    private GameRoomPresenter roomPresenter;
    private Presenter currentPresenter;

    public void switchToGameRoom(Room room) {
        if (roomPresenter == null) {
            roomPresenter = new GameRoomPresenter(roomView, room, this);
        }
        switchPresenter(roomPresenter, roomView);
    }

    public void switchToLobby() {
        if (lobbyPresenter == null) {
            lobbyPresenter = new GameLobbyPresenter(lobbyView, this);
        }
        switchPresenter(lobbyPresenter, lobbyView);
    }

    private void switchPresenter(Presenter newPresenter, Widget newView) {
        if (currentPresenter != null) {
            currentPresenter.stop();
        }
        RootPanel.get().clear();
        RootPanel.get().add(newView);
        currentPresenter = newPresenter;
        currentPresenter.start();
    }
}
