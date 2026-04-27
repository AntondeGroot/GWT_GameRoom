package ADG.Utils;

import ADG.UUID;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

import java.util.Collection;

public class Cookie {

    private static final String PLAYERID  = "playerid";
    private static final String USERNAME  = "username";

    public static String getPlayerId(){
        createPlayerIdCookie();
        return Cookies.getCookie(PLAYERID);
    }

    public static void createPlayerIdCookie(){
        Collection<String> cookieNames = Cookies.getCookieNames();
        if(!cookieNames.contains(PLAYERID)){
            Cookies.setCookie(PLAYERID, UUID.get(), null, null, "/", isSecure());
        }
    }

    public static String getUsername() {
        String value = Cookies.getCookie(USERNAME);
        return value != null ? value : "";
    }

    public static void setUsername(String username) {
        Cookies.setCookie(USERNAME, username, null, null, "/", isSecure());
    }

    private static boolean isSecure() {
        return Window.Location.getProtocol().startsWith("https");
    }
}
