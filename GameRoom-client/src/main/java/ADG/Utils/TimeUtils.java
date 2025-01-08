package ADG.Utils;

import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

public class TimeUtils {
    public static String getCurrentTime() {
        Date currentDate = new Date();
        DateTimeFormat timeFormat = DateTimeFormat.getFormat("HH:mm");
        return timeFormat.format(currentDate);
    }
}
