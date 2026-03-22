package ADG.Lobby;

import ADG.Presenter;
import ADG.PresenterManager;
import com.google.gwt.event.shared.HandlerRegistration;

public class GameOptionsPresenter implements Presenter {

    private final GameOptionsView view;
    private final Room room;
    private final PresenterManager presenterManager;
    private HandlerRegistration confirmReg;
    private HandlerRegistration cancelReg;

    public GameOptionsPresenter(GameOptionsView view, Room room, PresenterManager presenterManager) {
        this.view = view;
        this.room = room;
        this.presenterManager = presenterManager;
    }

    @Override
    public void start() {
        confirmReg = view.getConfirmButton().addClickHandler(e -> onConfirm());
        cancelReg  = view.getCancelButton().addClickHandler(e -> onCancel());
    }

    @Override
    public void stop() {
        if (confirmReg != null) { confirmReg.removeHandler(); confirmReg = null; }
        if (cancelReg  != null) { cancelReg.removeHandler();  cancelReg  = null; }
    }

    private void onConfirm() {
        room.setUniqueProfilePics(view.isUniqueProfilePics());
        presenterManager.switchToCharacterSelection(room);
    }

    private void onCancel() {
        presenterManager.switchToLobby();
    }
}
