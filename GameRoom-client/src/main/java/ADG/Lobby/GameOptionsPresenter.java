package ADG.Lobby;

import ADG.Presenter;
import ADG.PresenterManager;
import ADG.audio.AudioPlayer;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GameOptionsPresenter implements Presenter {

    private final GameOptionsView view;
    private final Room room;
    private final PresenterManager presenterManager;
    private final RoomServiceAsync roomService;
    private HandlerRegistration confirmReg;
    private HandlerRegistration cancelReg;

    public GameOptionsPresenter(GameOptionsView view, Room room, PresenterManager presenterManager, RoomServiceAsync roomService) {
        this.view = view;
        this.room = room;
        this.presenterManager = presenterManager;
        this.roomService = roomService;
    }

    @Override
    public void start() {
        view.init(room);
        confirmReg = view.getConfirmButton().addClickHandler(e -> { AudioPlayer.play(AudioPlayer.BUTTON_CLICK); onConfirm(); });
        cancelReg  = view.getCancelButton().addClickHandler(e -> { AudioPlayer.play(AudioPlayer.BUTTON_CLICK); onCancel(); });
    }

    @Override
    public void stop() {
        if (confirmReg != null) { confirmReg.removeHandler(); confirmReg = null; }
        if (cancelReg  != null) { cancelReg.removeHandler();  cancelReg  = null; }
    }

    private void onConfirm() {
        int maxPlayers = view.getMaxPlayers();
        if (maxPlayers < view.getMinBound() || maxPlayers > view.getMaxBound()) {
            Window.alert("Maximum players must be between " + view.getMinBound() + " and " + view.getMaxBound() + ".");
            return;
        }
        room.setMaxPlayers(maxPlayers);
        room.setUniqueProfilePics(view.isUniqueProfilePics());
        // Persist the options to the server before proceeding, so joining players see them
        roomService.updateRoom(room, new AsyncCallback<Void>() {
            @Override public void onFailure(Throwable t) {
                Window.alert("Failed to save room options: " + t.getMessage());
            }
            @Override public void onSuccess(Void v) {
                presenterManager.switchToCharacterSelection(room);
            }
        });
    }

    private void onCancel() {
        // Room was created as PENDING when the user clicked "Create Room" — clean it up
        roomService.deleteRoom(room.getId(), new AsyncCallback<Void>() {
            @Override public void onFailure(Throwable t) {}
            @Override public void onSuccess(Void v) {}
        });
        presenterManager.switchToLobby();
    }
}
