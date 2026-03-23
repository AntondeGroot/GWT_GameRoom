package ADG.Lobby;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class GameOptionsView extends Composite {

    interface Binder extends UiBinder<Widget, GameOptionsView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField CheckBox   uniqueProfilePicsCheckbox;
    @UiField FlowPanel  maxPlayersField;
    @UiField TextBox    maxPlayersInput;
    @UiField Label      maxPlayersRangeHint;
    @UiField Button     confirmButton;
    @UiField Button     cancelButton;

    private int minBound;
    private int maxBound;

    public GameOptionsView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void init(Room room) {
        uniqueProfilePicsCheckbox.setValue(room.isUniqueProfilePics());
        minBound = room.getMinPlayers();
        maxBound = room.getMaxPlayers();
        if (minBound == maxBound) {
            maxPlayersField.setVisible(false);
        } else {
            maxPlayersInput.setText(String.valueOf(maxBound));
            maxPlayersRangeHint.setText("(" + minBound + " – " + maxBound + ")");
        }
    }

    public boolean isUniqueProfilePics() { return uniqueProfilePicsCheckbox.getValue(); }

    /** Returns the entered value, or -1 if not a valid integer. */
    public int getMaxPlayers() {
        try {
            return Integer.parseInt(maxPlayersInput.getText().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public int getMinBound() { return minBound; }
    public int getMaxBound() { return maxBound; }

    public Button getConfirmButton() { return confirmButton; }
    public Button getCancelButton()  { return cancelButton; }
}