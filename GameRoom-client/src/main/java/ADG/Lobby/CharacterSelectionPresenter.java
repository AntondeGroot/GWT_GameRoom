package ADG.Lobby;

import ADG.*;
import ADG.Utils.Cookie;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashSet;
import java.util.Set;

public class CharacterSelectionPresenter implements Presenter {

    private static final int SPRITE_SHEET_TOTAL = 16;
    private static final int[] EXCLUDED_INDICES = {5, 9};
    private static final int SPRITE_SIZE = 100;
    private static final String SPRITE_SHEET_URL = "/profilepics.png";

    private final RoomServiceAsync roomService;
    private final CharacterSelectionView view;
    private final Room room;
    private final PresenterManager presenterManager;
    private int selectedProfileIndex = -1;
    private HandlerRegistration confirmReg;
    private HandlerRegistration cancelReg;

    public CharacterSelectionPresenter(CharacterSelectionView view, Room room, PresenterManager presenterManager, RoomServiceAsync roomService) {
        this.view = view;
        this.room = room;
        this.presenterManager = presenterManager;
        this.roomService = roomService;
    }

    @Override
    public void start() {
        History.newItem("joining=" + room.getId());
        confirmReg = view.getConfirmButton().addClickHandler(event -> onConfirm());
        cancelReg  = view.getCancelButton().addClickHandler(event -> onBackToLobby());
        loadProfilePictures();
    }

    @Override
    public void stop() {
        if (confirmReg != null) { confirmReg.removeHandler(); confirmReg = null; }
        if (cancelReg  != null) { cancelReg.removeHandler();  cancelReg  = null; }
    }

    private void loadProfilePictures() {
        view.getProfilePicGrid().clear();
        selectedProfileIndex = -1;
        view.getSelectedProfileLabel().setText("No profile picture selected");

        Set<Integer> takenIndices = new HashSet<>();
        if (room.isUniqueProfilePics()) {
            for (String profileId : room.getPlayerProfiles().values()) {
                try { takenIndices.add(Integer.parseInt(profileId)); } catch (NumberFormatException ignored) {}
            }
        }

        Canvas[] canvases = new Canvas[SPRITE_SHEET_TOTAL];
        for (int i = 0; i < SPRITE_SHEET_TOTAL; i++) {
            if (isExcluded(i)) continue;
            final int spriteIndex = i;
            final boolean taken = takenIndices.contains(i);
            Canvas canvas = Canvas.createIfSupported();
            canvas.setWidth(SPRITE_SIZE + "px");
            canvas.setHeight(SPRITE_SIZE + "px");
            canvas.setCoordinateSpaceWidth(SPRITE_SIZE);
            canvas.setCoordinateSpaceHeight(SPRITE_SIZE);
            canvas.setStyleName(taken ? "profile-pic profile-pic-taken" : "profile-pic");
            if (!taken) {
                canvas.addClickHandler(event -> onProfilePicSelected(spriteIndex, canvas));
            }
            view.getProfilePicGrid().add(canvas);
            canvases[i] = canvas;
        }

        Image img = new Image();
        img.setVisible(false);
        view.getProfilePicGrid().add(img);
        img.addLoadHandler((LoadEvent e) -> {
            ImageElement imgEl = ImageElement.as(img.getElement());
            double sw = 1024 / 4.0;
            double sh = 1024 / 4.0;
            for (int i = 0; i < SPRITE_SHEET_TOTAL; i++) {
                if (isExcluded(i)) continue;
                double sx = sw * (i % 4);
                double sy = sh * (i / 4);
                Context2d ctx = canvases[i].getContext2d();
                ctx.drawImage(imgEl, sx, sy, sw, sh, 0, 0, SPRITE_SIZE, SPRITE_SIZE);
                if (takenIndices.contains(i)) {
                    ctx.setFillStyle("rgba(10, 6, 20, 0.72)");
                    ctx.fillRect(0, 0, SPRITE_SIZE, SPRITE_SIZE);
                    ctx.setFillStyle("rgba(248, 113, 113, 0.9)");
                    ctx.setFont("bold 32px sans-serif");
                    ctx.setTextAlign("center");
                    ctx.setTextBaseline("middle");
                    ctx.fillText("\u2715", SPRITE_SIZE / 2.0, SPRITE_SIZE / 2.0);
                }
            }
        });
        img.setUrl(SPRITE_SHEET_URL);
    }

    private boolean isExcluded(int index) {
        for (int excluded : EXCLUDED_INDICES) {
            if (index == excluded) return true;
        }
        return false;
    }

    private void onProfilePicSelected(int index, Canvas selectedCanvas) {
        selectedProfileIndex = index;
        view.getSelectedProfileLabel().setText("Profile " + (index + 1) + " selected");
        clearCanvasSelection();
        selectedCanvas.addStyleName("profile-pic-selected");
    }

    private void clearCanvasSelection() {
        for (int i = 0; i < view.getProfilePicGrid().getWidgetCount(); i++) {
            Widget widget = view.getProfilePicGrid().getWidget(i);
            widget.removeStyleName("profile-pic-selected");
        }
    }

    private boolean isCreator() {
        return Cookie.getPlayerId().equals(room.getCreatedByUserId());
    }

    private void onConfirm() {
        String username = view.getUsernameInput().getText().trim();
        if (username.isEmpty()) {
            view.showAlert("Please enter a username.");
            return;
        }
        if (selectedProfileIndex == -1) {
            view.showAlert("Please select a profile picture.");
            return;
        }

        if (isCreator()) {
            roomService.createRoom(room, new AsyncCallback<Room>() {
                @Override
                public void onFailure(Throwable t) {
                    view.showAlert("Failed to create room: " + t.getMessage());
                }

                @Override
                public void onSuccess(Room created) {
                    roomService.addPlayerIdToRoom(Cookie.getPlayerId(), room, new AsyncCallback<Void>() {
                        @Override public void onFailure(Throwable t) {}
                        @Override public void onSuccess(Void v) {}
                    });
                    roomService.setUsernameAndProfile(room, Cookie.getPlayerId(), username, String.valueOf(selectedProfileIndex), new AsyncCallback<Void>() {
                        @Override public void onFailure(Throwable t) {}
                        @Override public void onSuccess(Void unused) {
                            presenterManager.switchToGameRoom(room);
                        }
                    });
                }
            });
        } else {
            roomService.setUsernameAndProfile(room, Cookie.getPlayerId(), username, String.valueOf(selectedProfileIndex), new AsyncCallback<Void>() {
                @Override public void onFailure(Throwable t) {}
                @Override public void onSuccess(Void unused) {
                    presenterManager.switchToGameRoom(room);
                }
            });
        }
    }

    private void onBackToLobby() {
        if (!isCreator()) {
            removePlayerFromRoom();
        }
        presenterManager.switchToLobby();
    }

    private void removePlayerFromRoom() {
        roomService.removePlayerFromRoom(Cookie.getPlayerId(), room, new AsyncCallback<Void>() {
            @Override public void onFailure(Throwable throwable) {}
            @Override public void onSuccess(Void v) {}
        });
    }
}
