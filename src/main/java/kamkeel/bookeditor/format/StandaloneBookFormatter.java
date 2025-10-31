package kamkeel.bookeditor.format;

/**
 * Default formatter that understands the vanilla section symbol based
 * formatting codes.
 */
public final class StandaloneBookFormatter extends BookFormatter {
    public static final StandaloneBookFormatter INSTANCE = new StandaloneBookFormatter();

    private StandaloneBookFormatter() {
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
            return 1; // dangling marker
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
        int candidate = endExclusive - 2;
        if (candidate >= 0 && text.charAt(candidate) == '\u00a7') {
            char codeChar = candidate + 1 < text.length() ? text.charAt(candidate + 1) : '\0';
            if (isFormatColor(codeChar) || isFormatSpecial(codeChar)) {
                return candidate;
            }
            return candidate + 1;
        }
        return -1;
    }

    @Override
    public String stripColorCodes(CharSequence text) {
        if (text == null) {
            return null;
        }
        StringBuilder stripped = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            int length = detectFormattingCodeLength(text, i);
            if (length > 0) {
                i += length - 1;
                continue;
            }
            stripped.append(text.charAt(i));
        }
        return stripped.toString();
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
                    color = "\u00a7" + c;
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
        if (k) out.append("\u00a7k");
        if (l) out.append("\u00a7l");
        if (m) out.append("\u00a7m");
        if (n) out.append("\u00a7n");
        if (o) out.append("\u00a7o");
        return out.toString();
    }

    @Override
    public String getFormatFromString(String text) {
        if (text == null) {
            return "";
        }
        String result = "";
        int index = -1;
        while ((index = text.indexOf('\u00a7', index + 1)) != -1) {
            if (index + 1 < text.length()) {
                char c0 = text.charAt(index + 1);
                if (isFormatColor(c0)) {
                    result = "\u00a7" + c0;
                    continue;
                }
                if (isFormatSpecial(c0)) {
                    result = result + "\u00a7" + c0;
                }
            }
        }
        return result;
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
                if (length < 2 || i + length > text.length()) {
                    break;
                }
                sanitized.append(text, i, i + length);
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
        char lower = Character.toLowerCase(code);
        return (lower >= 'k' && lower <= 'o') || lower == 'r';
    }
}
