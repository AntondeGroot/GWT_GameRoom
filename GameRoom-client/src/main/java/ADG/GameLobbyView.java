package ADG;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import java.util.List;

public class GameLobbyView extends Composite {

    interface GameLobbyViewUiBinder extends UiBinder<Widget, GameLobbyView> {}
    private final static GameLobbyViewUiBinder uiBinder = GWT.create(GameLobbyViewUiBinder.class);

    @UiField
    VerticalPanel mainPanel;

    @UiField
    HorizontalPanel createRoomPanel;

    @UiField
    Label createRoomLabel;

    @UiField
    TextBox roomNameInput;

    @UiField
    Button createRoomButton;

    @UiField
    Label availableRoomsHeader;

    @UiField
    CellTable<Room> roomTable;

    public GameLobbyView() {
        // Call UiBinder to initialize the layout
        initWidget(uiBinder.createAndBindUi(this));
    }

    public Button getCreateRoomButton() {
        return createRoomButton;
    }

    public TextBox getRoomNameInput() {
        return roomNameInput;
    }

    public CellTable<Room> getRoomTable() {
        return roomTable;
    }

    public void initializeTableHeaders(){
        // Add Room Name Column
        TextColumn<Room> roomNameColumn = new TextColumn<Room>() {
            @Override
            public String getValue(Room room) {
                return room.getName();
            }
        };

        // Add Number of Players Column
        TextColumn<Room> nrPlayersColumn = new TextColumn<Room>() {
            @Override
            public String getValue(Room room) {
                return room.getNrOfPlayers() + " / 8";
            }
        };

        // Add Status Column
        TextColumn<Room> statusColumn = new TextColumn<Room>() {
            @Override
            public String getValue(Room room) {
                switch (room.getStatus()) {
                    case PLAYING:
                        return "Playing";
                    case WAITING:
                        return "Waiting for players ...";
                    case FULL:
                        return "Full";
                    default:
                        return "";
                }
            }
        };

        // Add Join Button Column
        ButtonCell joinButtonCell = new StyledButtonCell("joinRoomButton");
        Column<Room, String> joinButtonColumn =
                new Column<Room, String>(joinButtonCell) {
                    @Override
                    public String getValue(Room room) {
                        return "Join";
                    }
                };


        // Add columns to the table
        roomTable.addColumn(roomNameColumn, "Room Name");
        roomTable.addColumn(nrPlayersColumn, "Players");
        roomTable.addColumn(statusColumn, "Status");
        roomTable.addColumn(joinButtonColumn, "");
    }

    public void showAlert(String msg){
        Window.alert(msg);
    }

    public void updateRoomTable(List<Room> rooms) {
        roomTable.setRowData(0, rooms);
        roomTable.setRowCount(rooms.size());
    }
}