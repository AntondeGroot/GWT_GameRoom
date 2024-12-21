package ADG;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class CharacterSelectionPresenter implements Presenter {

    private final GameRoomServiceAsync gameRoomService = GWT.create(GameRoomService.class);
    private final CharacterSelectionView view;
    private Room room; // the model
    private final PresenterManager presenterManager;
    private final String[] profilePicUrls = {
            "https://via.placeholder.com/100/FF0000?text=1",
            "https://via.placeholder.com/100/00FF00?text=2",
            "https://via.placeholder.com/100/0000FF?text=3",
            "https://via.placeholder.com/100/FFFF00?text=4",
            "https://via.placeholder.com/100/FF00FF?text=5",
            "https://via.placeholder.com/100/00FFFF?text=6"
    };
    private String selectedProfilePicUrl;

    public CharacterSelectionPresenter(CharacterSelectionView view, Room room, PresenterManager presenterManager) {
        this.view = view;
        this.room = room;
        this.presenterManager = presenterManager;
        bind();
        loadProfilePictures();
    }

    private void bind() {
        view.getConfirmButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onConfirm();
            }
        });

        view.getCancelButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onBackToLobby();
            }
        });
    }

    private void loadProfilePictures() {
        for (String picUrl : profilePicUrls) {
            Image profilePic = new Image(picUrl);
            profilePic.setStyleName("profile-pic");
            profilePic.addClickHandler(event -> onProfilePicSelected(picUrl, profilePic));
            view.getProfilePicGrid().add(profilePic);
        }
    }

    private void onProfilePicSelected(String picUrl, Image selectedImage) {
        selectedProfilePicUrl = picUrl;
        view.getSelectedProfileLabel().setText("Selected Profile Picture: " + picUrl);

        clearImageSelection();
        selectedImage.addStyleName("profile-pic-selected");
    }

    private void clearImageSelection() {
        for (int i = 0; i < view.getProfilePicGrid().getWidgetCount(); i++) {
            Widget widget = view.getProfilePicGrid().getWidget(i);
            if (widget instanceof Image) {
                widget.removeStyleName("profile-pic-selected");
            }
        }
    }

    private void onConfirm() {
        String username = view.getUsernameInput().getText().trim();
        if (username.isEmpty()) {
            Window.alert("Please enter a username.");
            return;
        }
        if (selectedProfilePicUrl == null) {
            Window.alert("Please select a profile picture.");
            return;
        }

        Window.alert("Username: " + username + "\nProfile Picture: " + selectedProfilePicUrl);
        presenterManager.switchToGameRoom(room);
        gameRoomService.setUsernameAndProfile(room, Cookie.getPlayerId() , username, selectedProfilePicUrl, new AsyncCallback<Void>() {
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
        gameRoomService.removePlayerFromRoom(Cookie.getPlayerId(), room, new AsyncCallback<Void>() {
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
