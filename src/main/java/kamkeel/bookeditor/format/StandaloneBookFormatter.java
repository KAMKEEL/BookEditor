package kamkeel.bookeditor.format;

/**
 * Default formatter that implements the vanilla Minecraft formatting rules used
 * before Hex Text integration.
 */
public class StandaloneBookFormatter extends BookFormatter {

    @Override
    public String stripColorCodes(CharSequence text) {
        if (text == null) {
            return null;
        }
        StringBuilder builder = null;
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '\u00a7') {
                if (builder == null) {
                    builder = new StringBuilder(text.length());
                    builder.append(text, 0, i);
                }
                if (i + 1 < text.length()) {
                    i++;
                }
                continue;
            }
            if (builder != null) {
                builder.append(current);
            }
        }
        return builder != null ? builder.toString() : text.toString();
    }

    @Override
    public int detectFormattingCodeLength(CharSequence text, int index) {
        if (text == null || index < 0 || index >= text.length()) {
            return 0;
        }
        if (text.charAt(index) != '\u00a7') {
            return 0;
        }
        if (index + 1 >= text.length()) {
            return 1;
        }
        char code = text.charAt(index + 1);
        if (isFormatColor(code) || isFormatSpecial(code)) {
            return 2;
        }
        return 1;
    }

    @Override
    public int findFormattingCodeStart(CharSequence text, int endExclusive) {
        if (text == null || endExclusive <= 0 || endExclusive > text.length()) {
            return -1;
        }
        if (endExclusive >= 2 && text.charAt(endExclusive - 2) == '\u00a7') {
            char codeChar = text.charAt(endExclusive - 1);
            if (isFormatColor(codeChar) || isFormatSpecial(codeChar)) {
                return endExclusive - 2;
            }
            return endExclusive - 1;
        }
        return -1;
    }

    @Override
    public String sanitizeFormatting(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sanitized = new StringBuilder(text.length());
        for (int i = 0; i < text.length();) {
            int length = detectFormattingCodeLength(text, i);
            if (length > 0) {
                if (length < 2) {
                    break;
                }
                sanitized.append(text, i, i + 2);
                i += length;
                continue;
            }
            sanitized.append(text.charAt(i));
            i++;
        }
        return sanitized.toString();
    }

    @Override
    public boolean isFormatColor(char code) {
        return (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f') || (code >= 'A' && code <= 'F');
    }

    @Override
    public boolean isFormatSpecial(char code) {
        return (code >= 'k' && code <= 'o') || (code >= 'K' && code <= 'O') || code == 'r' || code == 'R';
    }

    @Override
    public String getActiveFormatting(String text) {
        if (text == null) {
            return "";
        }
        String color = "";
        boolean k = false, l = false, m = false, n = false, o = false;
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '\u00a7') {
                char c = Character.toLowerCase(text.charAt(i + 1));
                if (c == 'r') {
                    color = "";
                    k = l = m = n = o = false;
                } else if (isFormatColor(c)) {
                    color = "§" + c;
                    k = l = m = n = o = false;
                } else if (c == 'k') {
                    k = true;
                } else if (c == 'l') {
                    l = true;
                } else if (c == 'm') {
                    m = true;
                } else if (c == 'n') {
                    n = true;
                } else if (c == 'o') {
                    o = true;
                }
            }
        }
        StringBuilder out = new StringBuilder();
        if (!color.isEmpty()) out.append(color);
        if (k) out.append("§k");
        if (l) out.append("§l");
        if (m) out.append("§m");
        if (n) out.append("§n");
        if (o) out.append("§o");
        return out.toString();
    }

    @Override
    public String getFormatFromString(String text) {
        if (text == null) {
            return "";
        }
        String s1 = "";
        int i = -1;
        int j = text.length();
        while ((i = text.indexOf('§', i + 1)) != -1) {
            if (i < j - 1) {
                char c0 = text.charAt(i + 1);
                if (isFormatColor(c0)) {
                    s1 = "§" + c0;
                    continue;
                }
                if (isFormatSpecial(c0)) {
                    s1 = s1 + "§" + c0;
                }
            }
        }
        return s1;
    }
}
