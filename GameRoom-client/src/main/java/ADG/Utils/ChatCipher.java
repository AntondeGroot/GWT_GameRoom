package ADG.Utils;

public class ChatCipher {

    //todo: this is just obfuscation not real encryption
    public static String encrypt(String text, String key) {
        if (key == null || key.isEmpty() || text == null || text.isEmpty()) return text;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            int xored = text.charAt(i) ^ key.charAt(i % key.length());
            sb.append(toHex(xored));
        }
        return sb.toString();
    }

    public static String decrypt(String hex, String key) {
        if (key == null || key.isEmpty() || hex == null || hex.isEmpty()) return hex;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i + 4 <= hex.length(); i += 4) {
            int xored = fromHex(hex.substring(i, i + 4));
            sb.append((char) (xored ^ key.charAt((i / 4) % key.length())));
        }
        return sb.toString();
    }

    private static String toHex(int v) {
        final String H = "0123456789abcdef";
        return "" + H.charAt((v >> 12) & 0xF) + H.charAt((v >> 8) & 0xF)
                  + H.charAt((v >> 4) & 0xF) + H.charAt(v & 0xF);
    }

    private static int fromHex(String s) {
        int r = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = (c >= '0' && c <= '9') ? c - '0'
                  : (c >= 'a' && c <= 'f') ? c - 'a' + 10
                  : (c >= 'A' && c <= 'F') ? c - 'A' + 10 : 0;
            r = r * 16 + d;
        }
        return r;
    }
}