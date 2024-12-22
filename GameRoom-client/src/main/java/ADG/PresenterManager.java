package ADG;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class PresenterManager {
    // Views
    private final GameLobbyView lobbyView = new GameLobbyView();
    private final GameRoomView roomView = new GameRoomView();
    private final CharacterSelectionView characterSelectionView = new CharacterSelectionView();
    // Presenters
    private GameLobbyPresenter lobbyPresenter;
    private GameRoomPresenter roomPresenter;
    private CharacterSelectionPresenter characterSelectionPresenter;
    private Presenter currentPresenter;
    // Services
    private final GameRoomServiceAsync gameRoomServiceAsync = GWT.create(GameRoomService.class);

    public void switchToGameRoom(Room room) {
        if (roomPresenter == null) {
            roomPresenter = new GameRoomPresenter(roomView, room, this, gameRoomServiceAsync);
        }
        switchPresenter(roomPresenter, roomView);
    }

    public void switchToLobby() {
        if (lobbyPresenter == null) {
            lobbyPresenter = new GameLobbyPresenter(lobbyView, this, gameRoomServiceAsync);
        }
        switchPresenter(lobbyPresenter, lobbyView);
    }

    public void switchToCharacterSelection(Room room){
        if(characterSelectionPresenter == null) {
            characterSelectionPresenter = new CharacterSelectionPresenter(characterSelectionView, room, this, gameRoomServiceAsync);
        }
        switchPresenter(characterSelectionPresenter, characterSelectionView);
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
