package ADG;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import java.util.HashMap;

class GameRoomView extends Composite {

    interface GameRoomUiBinder extends UiBinder<Widget, GameRoomView> {}
    private static GameRoomUiBinder uiBinder = GWT.create(GameRoomUiBinder.class);

    @UiField
    FlowPanel gameArea;

    @UiField
    VerticalPanel roomPanel;

    @UiField
    Label roomTitle;

    @UiField
    Button leaveRoomButton;

    @UiField
    Button deleteRoomButton;

    @UiField
    Button startGameButton;

    @UiField
    VerticalPanel playerPanel;

    @UiField
    Button sendMessageButton;

    @UiField
    TextArea messageDisplayField;

    @UiField
    TextArea messageInputField;

    public GameRoomView() {
        // Call UiBinder to initialize the layout
        initWidget(uiBinder.createAndBindUi(this));
    }

    public Label getRoomTitle() {
        return roomTitle;
    }

    public Button getLeaveRoomButton() {
        return leaveRoomButton;
    }

    public Button getDeleteRoomButton() {
        return deleteRoomButton;
    }

    public Button getStartGameButton() {
        return startGameButton;
    }

    public VerticalPanel getPlayerPanel() {return playerPanel;}

    public TextArea getMessageDisplayField() {return messageDisplayField;}

    public TextArea getMessageInputField() {return messageInputField;}

    public Button getSendMessageButton() {return sendMessageButton;}

    public void drawPlayerList(HashMap<String, String> userNames, HashMap<String, String> userProfiles) {
        playerPanel.clear();
        for (String userId : userNames.keySet()) {
            HorizontalPanel playerIndexPanel = new HorizontalPanel();
            String playerName = userNames.get(userId);
            String userProfile = userProfiles.get(userId);
            playerIndexPanel.add(new Label(playerName));
            playerIndexPanel.add(new Label(".  -  ."));
            playerIndexPanel.add(new Label(userProfile));
            playerPanel.add(playerIndexPanel);
        }
    }
}

