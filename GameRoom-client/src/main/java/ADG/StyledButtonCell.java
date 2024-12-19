package ADG;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class StyledButtonCell extends ButtonCell {

    private final String styleName;

    public StyledButtonCell(String styleName) {
        this.styleName = styleName;
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
        if (value != null) {
            sb.appendHtmlConstant("<button type=\"button\" class=\"" + styleName + "\">");
            sb.appendEscaped(value);
            sb.appendHtmlConstant("</button>");
        }
    }
}

