package ADG.Lobby;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class CharacterSelectionView extends Composite {

    interface Binder extends UiBinder<Widget, CharacterSelectionView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    VerticalPanel mainPanel;

    @UiField
    TextBox usernameInput;

    @UiField
    FlowPanel profilePicGrid;

    @UiField
    Label selectedProfileLabel;

    @UiField
    Button confirmButton, cancelButton;

    public CharacterSelectionView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public TextBox getUsernameInput() {
        return usernameInput;
    }

    public FlowPanel getProfilePicGrid() {
        return profilePicGrid;
    }

    public Label getSelectedProfileLabel() {
        return selectedProfileLabel;
    }

    public Button getConfirmButton() {
        return confirmButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public void showAlert(String message) {
        Window.alert(message);
    }
}
