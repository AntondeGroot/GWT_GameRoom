package ADG;

import ADG.Lobby.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class PresenterManager {
    // Views
    private final LobbyView lobbyView = new LobbyView();
    private final RoomView roomView = new RoomView();
    private final CharacterSelectionView characterSelectionView = new CharacterSelectionView();
    // Presenters
    private LobbyPresenter lobbyPresenter;
    private RoomPresenter roomPresenter;
    private CharacterSelectionPresenter characterSelectionPresenter;
    private Presenter currentPresenter;
    // Services
    private final GameRoomServiceAsync gameRoomServiceAsync = GWT.create(GameRoomService.class);
    private final MessageServiceAsync messageServiceAsync = GWT.create(MessageService.class);

    public void switchToGameRoom(Room room) {
        if (roomPresenter == null) {
            roomPresenter = new RoomPresenter(roomView, room, this, gameRoomServiceAsync, messageServiceAsync);
        }
        switchPresenter(roomPresenter, roomView);
    }

    public void switchToLobby() {
        if (lobbyPresenter == null) {
            lobbyPresenter = new LobbyPresenter(lobbyView, this, gameRoomServiceAsync);
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
