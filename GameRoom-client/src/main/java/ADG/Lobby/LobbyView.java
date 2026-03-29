package ADG.Lobby;

import ADG.audio.AudioPlayer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.List;

public class LobbyView extends Composite {

    interface GameLobbyViewUiBinder extends UiBinder<Widget, LobbyView> {}
    private final static GameLobbyViewUiBinder uiBinder = GWT.create(GameLobbyViewUiBinder.class);

    @UiField VerticalPanel mainPanel;
    @UiField FlowPanel createRoomPanel;
    @UiField TextBox roomNameInput;
    @UiField Button createRoomButton;
    @UiField Label availableRoomsHeader;
    @UiField ListBox gameListBox;
    @UiField FlowPanel roomTableContainer;

    public interface JoinHandler {
        void onJoin(Room room);
    }

    private JoinHandler joinHandler;
    private String currentPlayerId;
    private ArrayList<GameDefinition> gameDefinitions = new ArrayList<>();

    public LobbyView() {
        initWidget(uiBinder.createAndBindUi(this));
        buildTableHeader();
    }

    public Button getCreateRoomButton() { return createRoomButton; }
    public TextBox getRoomNameInput() { return roomNameInput; }

    public void setJoinHandler(JoinHandler handler) {
        this.joinHandler = handler;
    }

    public void setCurrentPlayerId(String playerId) {
        this.currentPlayerId = playerId;
    }

    public void populateGameList(ArrayList<GameDefinition> games) {
        gameDefinitions = games;
        gameListBox.clear();
        for (GameDefinition game : games) {
            gameListBox.addItem(game.getName(), game.getId());
        }
    }

    private String getGameName(String gameId) {
        for (GameDefinition def : gameDefinitions) {
            if (def.getId().equals(gameId)) return def.getName();
        }
        return gameId;
    }

    public String getSelectedGameId() {
        int index = gameListBox.getSelectedIndex();
        return index >= 0 ? gameListBox.getValue(index) : null;
    }

    public void showAlert(String msg) {
        Window.alert(msg);
    }

    public void updateRoomTable(List<Room> rooms) {
        // Keep header row (index 0), remove the rest
        while (roomTableContainer.getWidgetCount() > 1) {
            roomTableContainer.remove(1);
        }
        for (Room room : rooms) {
            roomTableContainer.add(buildRow(room));
        }
    }

    private void buildTableHeader() {
        FlowPanel header = new FlowPanel();
        header.setStyleName("room-table-header");
        header.add(makeHeaderCell("Room Name", "room-cell-name"));
        header.add(makeHeaderCell("Game", "room-cell-game"));
        header.add(makeHeaderCell("Players", "room-cell-players"));
        header.add(makeHeaderCell("Status", "room-cell-status"));
        header.add(makeHeaderCell("", "room-cell-action"));
        roomTableContainer.add(header);
    }

    private FlowPanel buildRow(Room room) {
        FlowPanel row = new FlowPanel();
        row.setStyleName("room-table-row");

        Label nameLabel = new Label(room.getName());
        nameLabel.setStyleName("room-table-cell room-cell-name");
        row.add(nameLabel);

        Label gameLabel = new Label(getGameName(room.getGameId()));
        gameLabel.setStyleName("room-table-cell room-cell-game");
        row.add(gameLabel);

        Label playersLabel = new Label(room.getNrOfPlayers() + " / " + room.getMaxPlayers());
        playersLabel.setStyleName("room-table-cell room-cell-players");
        row.add(playersLabel);

        Label statusLabel = new Label(getStatusText(room.getStatus()));
        statusLabel.setStyleName("room-table-cell room-cell-status " + getStatusClass(room.getStatus()));
        row.add(statusLabel);

        FlowPanel actionCell = new FlowPanel();
        actionCell.setStyleName("room-table-cell room-cell-action");
        boolean isMember = currentPlayerId != null && room.getPlayerIds().contains(currentPlayerId);
        if (isMember) {
            Button rejoinBtn = new Button("Rejoin");
            rejoinBtn.setStylePrimaryName("joinRoomButton");
            rejoinBtn.addClickHandler(e -> { AudioPlayer.play(AudioPlayer.BUTTON_CLICK); if (joinHandler != null) joinHandler.onJoin(room); });
            actionCell.add(rejoinBtn);
        } else if (GameStatus.WAITING.equals(room.getStatus())) {
            Button joinBtn = new Button("Join");
            joinBtn.setStylePrimaryName("joinRoomButton");
            joinBtn.addClickHandler(e -> { AudioPlayer.play(AudioPlayer.BUTTON_CLICK); if (joinHandler != null) joinHandler.onJoin(room); });
            actionCell.add(joinBtn);
        }
        row.add(actionCell);

        return row;
    }

    private Label makeHeaderCell(String text, String extraStyle) {
        Label label = new Label(text);
        label.setStyleName("room-table-header-cell " + extraStyle);
        return label;
    }

    private String getStatusText(GameStatus status) {
        switch (status) {
            case PLAYING: return "Playing";
            case WAITING: return "Waiting...";
            case FULL:    return "Full";
            default:      return "";
        }
    }

    private String getStatusClass(GameStatus status) {
        switch (status) {
            case PLAYING: return "status-playing";
            case WAITING: return "status-waiting";
            case FULL:    return "status-full";
            default:      return "";
        }
    }
}