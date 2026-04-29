package ADG.Lobby;

import ADG.Presenter;
import ADG.PresenterManager;
import ADG.Utils.Cookie;
import ADG.Utils.GameTranslations;
import ADG.audio.AudioPlayer;
import ADG.i18n.I18n;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.HashMap;

public class GameOptionsPresenter implements Presenter {

    private final GameOptionsView view;
    private final Room room;
    private final PresenterManager presenterManager;
    private final RoomServiceAsync roomService;
    private HandlerRegistration confirmReg;
    private HandlerRegistration cancelReg;
    private final ArrayList<GameOption> preloadedOptions;

    public GameOptionsPresenter(GameOptionsView view, Room room, PresenterManager presenterManager, RoomServiceAsync roomService) {
        this(view, room, presenterManager, roomService, null);
    }

    public GameOptionsPresenter(GameOptionsView view, Room room, PresenterManager presenterManager, RoomServiceAsync roomService, ArrayList<GameOption> preloadedOptions) {
        this.view = view;
        this.room = room;
        this.presenterManager = presenterManager;
        this.roomService = roomService;
        this.preloadedOptions = preloadedOptions;
    }

    @Override
    public void start() {
        view.init(room);
        confirmReg = view.getConfirmButton().addClickHandler(e -> { AudioPlayer.play(AudioPlayer.BUTTON_CLICK); onConfirm(); });
        cancelReg  = view.getCancelButton().addClickHandler(e -> { AudioPlayer.play(AudioPlayer.BUTTON_CLICK); onCancel(); });
        if (preloadedOptions != null) {
            showOptionsAfterLoadingTranslations(preloadedOptions);
        } else if (room.getGameId() != null) {
            roomService.getGameOptions(room.getGameId(), new AsyncCallback<ArrayList<GameOption>>() {
                @Override public void onFailure(Throwable t) {}
                @Override public void onSuccess(ArrayList<GameOption> options) {
                    showOptionsAfterLoadingTranslations(options);
                }
            });
        }
    }

    private void showOptionsAfterLoadingTranslations(ArrayList<GameOption> options) {
        GameTranslations.load(room.getGameBaseUrl(), Cookie.getLanguage(), () ->
            view.showGameSpecificOptions(options)
        );
    }

    @Override
    public void stop() {
        if (confirmReg != null) { confirmReg.removeHandler(); confirmReg = null; }
        if (cancelReg  != null) { cancelReg.removeHandler();  cancelReg  = null; }
    }

    private void onConfirm() {
        int maxPlayers = view.getMaxPlayers();
        if (maxPlayers < view.getMinBound() || maxPlayers > view.getMaxBound()) {
            Window.alert(I18n.m().errMaxPlayersBetween(view.getMinBound(), view.getMaxBound()));
            return;
        }
        room.setMaxPlayers(maxPlayers);
        room.setUniqueProfilePics(view.isUniqueProfilePics());
        HashMap<String, String> gameOpts = view.collectGameOptions();
        if (gameOpts != null) {
            room.setGameOptions(gameOpts);
        }
        // Persist the options to the server before proceeding, so joining players see them
        GWT.log("Updating room with ID: " + room.getId());
        roomService.updateRoom(room, new AsyncCallback<Void>() {
            @Override public void onFailure(Throwable t) {
                String errorMsg = "Unknown error";
                if (t instanceof ADG.Lobby.RoomServiceException) {
                    errorMsg = t.getMessage();
                } else if (t.getMessage() != null) {
                    errorMsg = t.getMessage();
                }
                GWT.log("Failed to update room: " + errorMsg);
                Window.alert("Failed to save game options: " + errorMsg);
            }
            @Override public void onSuccess(Void v) {
                GWT.log("Room updated successfully, switching to character selection");
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
