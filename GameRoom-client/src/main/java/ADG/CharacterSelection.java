package ADG;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

public class CharacterSelection {

    private final GameRoomServiceAsync gameRoomService = GWT.create(GameRoomService.class);
    private final Room room;

    private VerticalPanel mainPanel = new VerticalPanel();
    private TextBox usernameInput = new TextBox();
    private Button confirmButton = new Button("Confirm");
    private Button cancelButton = new Button("Back to Lobby");
    private Label selectedProfileLabel = new Label("No profile picture selected");
    private String selectedProfilePicUrl = null; // Store the selected profile picture URL

    // List of available profile picture URLs (you can replace these with actual image URLs)
    private String[] profilePicUrls = {
            "https://via.placeholder.com/100/FF0000?text=1",
            "https://via.placeholder.com/100/00FF00?text=2",
            "https://via.placeholder.com/100/0000FF?text=3",
            "https://via.placeholder.com/100/FFFF00?text=4",
            "https://via.placeholder.com/100/FF00FF?text=5",
            "https://via.placeholder.com/100/00FFFF?text=6"
    };

    public CharacterSelection(Room room) {
        this.room = room;
    }

    public void load() {
        RootPanel.get().add(mainPanel);

        // Title
        Label title = new Label("Character Selection");
        title.setStyleName("title");
        mainPanel.add(title);

        // Username input section
        HorizontalPanel usernamePanel = new HorizontalPanel();
        usernamePanel.add(new Label("Enter Username:"));
        usernameInput.getElement().setPropertyString("placeholder", "Enter your username");
        usernamePanel.add(usernameInput);
        mainPanel.add(usernamePanel);

        // Profile picture selection section
        Label selectProfileLabel = new Label("Select Your Profile Picture:");
        mainPanel.add(selectProfileLabel);

        // Add images for profile picture selection
        FlowPanel imageGrid = new FlowPanel();
        imageGrid.setStyleName("profile-pic-grid");

        for (String picUrl : profilePicUrls) {
            Image profilePic = new Image(picUrl);
            profilePic.setStyleName("profile-pic");
            profilePic.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    onProfilePicSelected(picUrl, profilePic);
                }
            });
            imageGrid.add(profilePic);
        }

        mainPanel.add(imageGrid);

        // Label to show which profile picture was selected
        mainPanel.add(selectedProfileLabel);

        // Confirm button to finalize selection
        confirmButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onConfirm();
            }
        });
        mainPanel.add(confirmButton);

        // Cancel button to go back to the lobby
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onBackToLobby();
            }
        });
        mainPanel.add(cancelButton);



        RootPanel.get().add(mainPanel);
    }

    /**
     * Handles when a profile picture is clicked.
     *
     * @param picUrl The URL of the selected profile picture.
     * @param selectedImage The Image widget that was clicked.
     */
    private void onProfilePicSelected(String picUrl, Image selectedImage) {
        this.selectedProfilePicUrl = picUrl;
        selectedProfileLabel.setText("Selected Profile Picture: " + picUrl);

        // Highlight the selected image
        clearImageSelection();
        selectedImage.addStyleName("profile-pic-selected");
    }

    /**
     * Clear the highlight on all images in the image grid.
     */
    private void clearImageSelection() {
        for (int i = 0; i < mainPanel.getWidgetCount(); i++) {
            Widget widget = mainPanel.getWidget(i);
            if (widget instanceof FlowPanel) {
                FlowPanel imageGrid = (FlowPanel) widget;
                for (int j = 0; j < imageGrid.getWidgetCount(); j++) {
                    Widget imageWidget = imageGrid.getWidget(j);
                    if (imageWidget instanceof Image) {
                        imageWidget.removeStyleName("profile-pic-selected");
                    }
                }
            }
        }
    }

    /**
     * Handles the confirmation of the user's selection.
     */
    private void onConfirm() {
        String username = usernameInput.getText().trim();
        if (username.isEmpty()) {
            Window.alert("Please enter a username.");
            return;
        }

        if (selectedProfilePicUrl == null) {
            Window.alert("Please select a profile picture.");
            return;
        }

        Window.alert("Username: " + username + "\nProfile Picture: " + selectedProfilePicUrl);

        gameRoomService.getRoomById(room.getId(), new AsyncCallback<Room>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Room room) {
                gameRoomService.setUsernameAndProfile(room, Cookie.getPlayerId(), username, selectedProfilePicUrl, new AsyncCallback() {
                    @Override
                    public void onFailure(Throwable throwable) {
                    }

                    @Override
                    public void onSuccess(Object o) {
                        navigateToRoom(room);
                    }
                });

            }
        });
    }

    private void onBackToLobby() {
        RootPanel.get().clear();
        GameLobby gameLobby = new GameLobby();
        gameLobby.onModuleLoad();
        removePlayerFromRoom();
    }

    private void navigateToRoom(Room room) {
        RootPanel.get().clear();
        GameRoom gameRoom = new GameRoom(room);
        gameRoom.load();
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
}
