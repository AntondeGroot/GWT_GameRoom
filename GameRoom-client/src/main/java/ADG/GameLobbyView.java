package ADG;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;

public class GameLobbyView extends Composite {

    interface GameLobbyViewUiBinder extends UiBinder<Widget, GameLobbyView> {}
    private static GameLobbyViewUiBinder uiBinder = GWT.create(GameLobbyViewUiBinder.class);

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
}