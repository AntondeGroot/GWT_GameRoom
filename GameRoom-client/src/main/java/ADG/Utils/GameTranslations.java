package ADG.Utils;

import com.google.gwt.http.client.*;
import com.google.gwt.json.client.*;
import java.util.HashMap;

public class GameTranslations {

    private static final HashMap<String, String> translations = new HashMap<>();

    public static void load(String baseUrl, Language lang, Runnable onComplete) {
        String url = baseUrl + "/i18n/" + lang.name() + ".json";
        try {
            new RequestBuilder(RequestBuilder.GET, url).sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request req, Response res) {
                    if (res.getStatusCode() == 200) {
                        translations.clear();
                        flatten("", JSONParser.parseStrict(res.getText()).isObject());
                    }
                    onComplete.run();
                }
                public void onError(Request req, Throwable t) {
                    onComplete.run();
                }
            });
        } catch (RequestException e) {
            onComplete.run();
        }
    }

    private static void flatten(String prefix, JSONObject obj) {
        if (obj == null) return;
        for (String k : obj.keySet()) {
            String fullKey = prefix.isEmpty() ? k : prefix + "." + k;
            JSONValue val = obj.get(k);
            if (val.isString() != null) {
                translations.put(fullKey, val.isString().stringValue());
            } else if (val.isObject() != null) {
                flatten(fullKey, val.isObject());
            }
        }
    }

    public static String translate(String key) {
        String v = translations.get(key);
        return v != null ? v : key;
    }
}