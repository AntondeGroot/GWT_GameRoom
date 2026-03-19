package ADG.Lobby;

import ADG.*;
import ADG.Utils.Cookie;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

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

    public CharacterSelectionPresenter(CharacterSelectionView view, Room room, PresenterManager presenterManager, RoomServiceAsync roomService) {
        this.view = view;
        this.room = room;
        this.presenterManager = presenterManager;
        this.roomService = roomService;
        bind();
        loadProfilePictures();
    }

    private void bind() {
        view.getConfirmButton().addClickHandler(event -> onConfirm());
        view.getCancelButton().addClickHandler(event -> onBackToLobby());
    }

    private void loadProfilePictures() {
        view.getProfilePicGrid().clear();
        selectedProfileIndex = -1;
        view.getSelectedProfileLabel().setText("No profile picture selected");

        Canvas[] canvases = new Canvas[SPRITE_SHEET_TOTAL];
        for (int i = 0; i < SPRITE_SHEET_TOTAL; i++) {
            if (isExcluded(i)) continue;
            final int spriteIndex = i;
            Canvas canvas = Canvas.createIfSupported();
            canvas.setWidth(SPRITE_SIZE + "px");
            canvas.setHeight(SPRITE_SIZE + "px");
            canvas.setCoordinateSpaceWidth(SPRITE_SIZE);
            canvas.setCoordinateSpaceHeight(SPRITE_SIZE);
            canvas.setStyleName("profile-pic");
            canvas.addClickHandler(event -> onProfilePicSelected(spriteIndex, canvas));
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
                canvases[i].getContext2d().drawImage(imgEl, sx, sy, sw, sh, 0, 0, SPRITE_SIZE, SPRITE_SIZE);
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

        presenterManager.switchToGameRoom(room);
        roomService.setUsernameAndProfile(room, Cookie.getPlayerId(), username, String.valueOf(selectedProfileIndex), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Void unused) {
            }
        });
    }

    private void onBackToLobby() {
        presenterManager.switchToLobby();
        removePlayerFromRoom();
    }

    private void removePlayerFromRoom() {
        roomService.removePlayerFromRoom(Cookie.getPlayerId(), room, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Void v) {
            }
        });
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}