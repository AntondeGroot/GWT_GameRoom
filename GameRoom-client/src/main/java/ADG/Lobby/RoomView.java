package ADG.Lobby;

import ADG.Utils.Cookie;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.HashMap;

public class RoomView extends Composite {

    interface RoomUiBinder extends UiBinder<Widget, RoomView> {}
    private static RoomUiBinder uiBinder = GWT.create(RoomUiBinder.class);

    @UiField
    VerticalPanel roomPanel;

    @UiField
    HeadingElement roomTitle;

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

    public RoomView() {
        // Call UiBinder to initialize the layout
        initWidget(uiBinder.createAndBindUi(this));
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

    public void refreshPlayerList(HashMap<String, String> userNames, HashMap<String, String> userProfiles) {
        playerPanel.clear();
        for (String userId : userNames.keySet()) {
            HorizontalPanel playerIndexPanel = new HorizontalPanel();
            String playerName = userNames.get(userId);
            String playerProfileUrl = userProfiles.get(userId);
            // create image
            Image profilePic = new Image(playerProfileUrl);
            profilePic.setStyleName("profile-pic-small");
            profilePic.setWidth("50px");
            // create player label
            playerIndexPanel.add(profilePic);
            Label playerNameLabel = new Label(playerName);
            playerIndexPanel.add(playerNameLabel);

            playerPanel.add(playerIndexPanel);
        }
    }
    public void updateCreatorControls(Room room){
        startGameButton.setEnabled(displayButtonForCreatorOfRoom(room));
        startGameButton.setVisible(displayButtonForCreatorOfRoom(room));
        deleteRoomButton.setEnabled(displayButtonForCreatorOfRoom(room));
        deleteRoomButton.setVisible(displayButtonForCreatorOfRoom(room));
    }

    private boolean displayButtonForCreatorOfRoom(Room room){
        if(!room.getCreatedByUserId().equals(Cookie.getPlayerId())){
            return false;
        }
        if(room.getStatus() == GameStatus.PLAYING){
            return false;
        }
        return true;
    }

    public void refreshMessages(ArrayList<Message> messages){
        StringBuilder output = new StringBuilder();
        String lastTimeStamp = "";
        for(Message message : messages){
            if(!lastTimeStamp.equals(message.getTimestamp())){
                lastTimeStamp = message.getTimestamp();
                output.append(lastTimeStamp);
            }else{
                output.append("     ");
            }
            output.append(" ").append(message.getNameSender()).append(" : ").append(message.getMessage());
            output.append("\n");
        }
        messageDisplayField.setText(output.toString());
    }

    public void showRoomName(String roomName){
        roomTitle.setInnerText("Room : " + roomName);
    }
}

