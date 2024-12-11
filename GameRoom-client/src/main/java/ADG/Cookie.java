package ADG;

import com.google.gwt.user.client.Cookies;

import java.util.Collection;

public class Cookie {

    private static final String PLAYERID = "playerid";

    public static String getPlayerId(){
        createPlayerIdCookie();
        return Cookies.getCookie(PLAYERID);
    }

    public static void createPlayerIdCookie(){
        Collection<String> cookieNames = Cookies.getCookieNames();
        if(!cookieNames.contains(PLAYERID)){
            Cookies.setCookie(PLAYERID, UUID.get());
        }
    }
}
