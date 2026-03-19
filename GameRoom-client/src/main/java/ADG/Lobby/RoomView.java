package ADG.Lobby;

import ADG.Utils.Cookie;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.LoadEvent;
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
    Label startInfoLabel;

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
            String profileIndex = userProfiles.get(userId);

            Canvas canvas = buildProfileCanvas(profileIndex);
            playerIndexPanel.add(canvas);
            playerIndexPanel.add(new Label(playerName));
            playerPanel.add(playerIndexPanel);
        }
    }

    private Canvas buildProfileCanvas(String profileIndex) {
        int size = 50;
        int index = 0;
        if (profileIndex != null) {
            try { index = Integer.parseInt(profileIndex); } catch (NumberFormatException ignored) {}
        }
        final int spriteIndex = index;

        Canvas canvas = Canvas.createIfSupported();
        canvas.setWidth(size + "px");
        canvas.setHeight(size + "px");
        canvas.setCoordinateSpaceWidth(size);
        canvas.setCoordinateSpaceHeight(size);
        canvas.setStyleName("profile-pic-small");

        Image img = new Image();
        img.setVisible(false);
        playerPanel.add(img);
        img.addLoadHandler((LoadEvent e) -> {
            ImageElement imgEl = ImageElement.as(img.getElement());
            double sw = 1024 / 4.0;
            double sh = 1024 / 4.0;
            double sx = sw * (spriteIndex % 4);
            double sy = sh * (spriteIndex / 4);
            Context2d ctx = canvas.getContext2d();
            ctx.drawImage(imgEl, sx, sy, sw, sh, 0, 0, size, size);
            img.removeFromParent();
        });
        img.setUrl("/profilepics.png");

        return canvas;
    }
    public void updateCreatorControls(Room room) {
        boolean isCreator = room.getCreatedByUserId().equals(Cookie.getPlayerId())
                && room.getStatus() != GameStatus.PLAYING;
        boolean enoughPlayers = room.getPlayerNames().size() >= room.getMinPlayers();

        startGameButton.setVisible(isCreator);
        startGameButton.setEnabled(isCreator && enoughPlayers);

        if (isCreator) {
            if (!enoughPlayers) {
                int missing = room.getMinPlayers() - room.getPlayerNames().size();
                startInfoLabel.setText("Waiting for " + missing + " more player" + (missing == 1 ? "" : "s") + " to join.");
                startInfoLabel.setStyleName("startInfoLabel startInfoLabel-waiting");
            } else {
                startInfoLabel.setText("");
            }
        } else {
            String creatorName = room.getPlayerNames().get(room.getCreatedByUserId());
            if (creatorName == null) creatorName = "the host";
            startInfoLabel.setText(creatorName + " will start the game.");
            startInfoLabel.setStyleName("startInfoLabel startInfoLabel-waiting");
        }

        deleteRoomButton.setEnabled(isCreator);
        deleteRoomButton.setVisible(isCreator);
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

