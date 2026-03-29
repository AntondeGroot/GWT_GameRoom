package ADG.Lobby;

import ADG.*;
import ADG.audio.AudioPlayer;
import ADG.Utils.Cookie;
import ADG.Utils.PollingService;
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

    private static final int SPRITE_SIZE        = 100;
    private static final int POLLING_INTERVAL_MS = 500;

    private final RoomServiceAsync roomService;
    private final CharacterSelectionView view;
    private final Room room;
    private final PresenterManager presenterManager;
    private int selectedProfileIndex = -1;
    private Canvas[] canvases = new Canvas[0];
    private HandlerRegistration confirmReg;
    private HandlerRegistration cancelReg;
    private final PollingService pollingService = new PollingService();

    public CharacterSelectionPresenter(CharacterSelectionView view, Room room,
                                       PresenterManager presenterManager,
                                       RoomServiceAsync roomService) {
        this.view             = view;
        this.room             = room;
        this.presenterManager = presenterManager;
        this.roomService      = roomService;
    }

    @Override
    public void start() {
        History.newItem("joining=" + room.getId());
        confirmReg = view.getConfirmButton().addClickHandler(event -> onConfirm());
        cancelReg  = view.getCancelButton().addClickHandler(event -> { AudioPlayer.play(AudioPlayer.BUTTON_CLICK); onBackToLobby(); });
        selectedProfileIndex = -1;
        view.getSelectedProfileLabel().setText("No profile picture selected");
        view.getUsernameInput().setText(ADG.Utils.Cookie.getUsername());
        loadProfilePictures();
        if (room.isUniqueProfilePics()) {
            pollingService.startPolling(POLLING_INTERVAL_MS, this::pollServerForProfileUpdates);
        }
    }

    @Override
    public void stop() {
        pollingService.stopPolling();
        if (confirmReg != null) { confirmReg.removeHandler(); confirmReg = null; }
        if (cancelReg  != null) { cancelReg.removeHandler();  cancelReg  = null; }
    }

    private void pollServerForProfileUpdates() {
        roomService.getRoomById(room.getId(), new AsyncCallback<Room>() {
            @Override
            public void onFailure(Throwable t) {}

            @Override
            public void onSuccess(Room updatedRoom) {
                if (updatedRoom == null) {
                    pollingService.stopPolling();
                    presenterManager.switchToLobby();
                    return;
                }
                if (!updatedRoom.getPlayerProfiles().equals(room.getPlayerProfiles())) {
                    String myId = ADG.Utils.Cookie.getPlayerId();
                    boolean selectionTaken = selectedProfileIndex != -1
                            && updatedRoom.getPlayerProfiles().entrySet().stream()
                                         .filter(e -> !e.getKey().equals(myId))
                                         .anyMatch(e -> e.getValue().equals(String.valueOf(selectedProfileIndex)));
                    room.getPlayerProfiles().clear();
                    room.getPlayerProfiles().putAll(updatedRoom.getPlayerProfiles());
                    if (selectionTaken) {
                        selectedProfileIndex = -1;
                        view.getSelectedProfileLabel().setText("No profile picture selected");
                        view.showAlert("Your selected profile was taken by another player.");
                    }
                    loadProfilePictures();
                }
            }
        });
    }

    private void loadProfilePictures() {
        view.getProfilePicGrid().clear();
        canvases = new Canvas[SpriteSheets.totalSprites()];

        Set<Integer> takenIndices = new HashSet<>();
        if (room.isUniqueProfilePics()) {
            String myId = ADG.Utils.Cookie.getPlayerId();
            for (java.util.Map.Entry<String, String> entry : room.getPlayerProfiles().entrySet()) {
                if (entry.getKey().equals(myId)) continue;
                try { takenIndices.add(Integer.parseInt(entry.getValue())); } catch (NumberFormatException ignored) {}
            }
        }

        int globalOffset = 0;
        for (SpriteSheets.Sheet sheet : SpriteSheets.all()) {
            loadSheet(sheet, globalOffset, takenIndices);
            globalOffset += sheet.total();
        }
    }

    private void loadSheet(SpriteSheets.Sheet sheet, int globalOffset, Set<Integer> takenIndices) {
        // Create canvas elements for every non-excluded sprite in this sheet
        for (int local = 0; local < sheet.total(); local++) {
            if (sheet.isExcluded(local)) continue;
            final int globalIndex = globalOffset + local;
            final boolean taken = takenIndices.contains(globalIndex);
            Canvas canvas = Canvas.createIfSupported();
            canvas.setWidth(SPRITE_SIZE + "px");
            canvas.setHeight(SPRITE_SIZE + "px");
            canvas.setCoordinateSpaceWidth(SPRITE_SIZE);
            canvas.setCoordinateSpaceHeight(SPRITE_SIZE);
            canvas.setStyleName(taken ? "profile-pic profile-pic-taken" : "profile-pic");
            if (!taken) {
                canvas.addClickHandler(event -> onProfilePicSelected(globalIndex, canvas));
            }
            view.getProfilePicGrid().add(canvas);
            canvases[globalIndex] = canvas;
        }

        // Load the sprite sheet image and draw each sprite into its canvas
        Image img = new Image();
        img.setVisible(false);
        view.getProfilePicGrid().add(img);
        img.addLoadHandler((LoadEvent e) -> {
            ImageElement imgEl = ImageElement.as(img.getElement());
            for (int local = 0; local < sheet.total(); local++) {
                if (sheet.isExcluded(local)) continue;
                int globalIndex = globalOffset + local;
                int localCol = local % sheet.cols;
                int localRow = local / sheet.cols;
                Context2d ctx = canvases[globalIndex].getContext2d();
                ctx.drawImage(imgEl,
                        sheet.srcX(localCol), sheet.srcY(localRow), sheet.srcW(), sheet.srcH(),
                        0, 0, SPRITE_SIZE, SPRITE_SIZE);
                if (takenIndices.contains(globalIndex)) {
                    ctx.setFillStyle("rgba(10, 6, 20, 0.72)");
                    ctx.fillRect(0, 0, SPRITE_SIZE, SPRITE_SIZE);
                    ctx.setFillStyle("rgba(248, 113, 113, 0.9)");
                    ctx.setFont("bold 32px sans-serif");
                    ctx.setTextAlign("center");
                    ctx.setTextBaseline("middle");
                    ctx.fillText("\u2715", SPRITE_SIZE / 2.0, SPRITE_SIZE / 2.0);
                }
            }
            // Re-apply selection highlight after redraw
            if (selectedProfileIndex != -1 && canvases[selectedProfileIndex] != null) {
                canvases[selectedProfileIndex].addStyleName("profile-pic-selected");
            }
        });
        img.setUrl(sheet.url);
    }

    private void onProfilePicSelected(int globalIndex, Canvas selectedCanvas) {
        selectedProfileIndex = globalIndex;
        view.getSelectedProfileLabel().setText("");
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
            AudioPlayer.errorAlert("Please enter a username.");
            return;
        }
        if (selectedProfileIndex == -1) {
            AudioPlayer.errorAlert("Please select a profile picture.");
            return;
        }
        AudioPlayer.play(AudioPlayer.BUTTON_CLICK);

        Cookie.setUsername(username);

        if (isCreator()) {
            roomService.addPlayerIdToRoom(Cookie.getPlayerId(), room.getId(), new AsyncCallback<Void>() {
                @Override public void onFailure(Throwable t) {
                    view.showAlert("Failed to join room: " + t.getMessage());
                }
                @Override public void onSuccess(Void v) {}
            });
            roomService.setUsernameAndProfile(room, Cookie.getPlayerId(), username,
                    String.valueOf(selectedProfileIndex), new AsyncCallback<Void>() {
                @Override public void onFailure(Throwable t) {
                    view.showAlert("Failed to set profile: " + t.getMessage());
                }
                @Override public void onSuccess(Void unused) {
                    roomService.publishRoom(room.getId(), new AsyncCallback<Void>() {
                        @Override public void onFailure(Throwable t) {
                            view.showAlert("Failed to open room: " + t.getMessage());
                        }
                        @Override public void onSuccess(Void v) {
                            presenterManager.switchToGameRoom(room);
                        }
                    });
                }
            });
        } else {
            roomService.setUsernameAndProfile(room, Cookie.getPlayerId(), username,
                    String.valueOf(selectedProfileIndex), new AsyncCallback<Void>() {
                @Override public void onFailure(Throwable t) {
                    view.showAlert("Failed to set profile: " + t.getMessage());
                }
                @Override public void onSuccess(Void unused) {
                    presenterManager.switchToGameRoom(room);
                }
            });
        }
    }

    private void onBackToLobby() {
        if (isCreator()) {
            roomService.deleteRoom(room.getId(), new AsyncCallback<Void>() {
                @Override public void onFailure(Throwable t) {}
                @Override public void onSuccess(Void v) {}
            });
        } else {
            roomService.removePlayerFromRoom(Cookie.getPlayerId(), room.getId(), new AsyncCallback<Void>() {
                @Override public void onFailure(Throwable throwable) {}
                @Override public void onSuccess(Void v) {}
            });
        }
        presenterManager.switchToLobby();
    }
}