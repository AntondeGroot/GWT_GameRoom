package ADG.Lobby;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class GameOptionsView extends Composite {

    interface Binder extends UiBinder<Widget, GameOptionsView> {}
    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField CheckBox uniqueProfilePicsCheckbox;
    @UiField Button confirmButton;
    @UiField Button cancelButton;

    public GameOptionsView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public boolean isUniqueProfilePics() { return uniqueProfilePicsCheckbox.getValue(); }
    public Button getConfirmButton()     { return confirmButton; }
    public Button getCancelButton()      { return cancelButton; }
}