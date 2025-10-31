package kamkeel.bookeditor.format;

/**
 * Default formatter that mirrors vanilla Minecraft's behaviour. This is the
 * implementation used when HexText is not present.
 */
public final class StandaloneBookFormatter extends BookFormatter {
    @Override
    public int detectFormattingCodeLength(CharSequence text, int index) {
        if (text == null || index < 0 || index >= text.length()) {
            return 0;
        }
        if (text.charAt(index) != SECTION_SIGN) {
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
        int candidate = endExclusive - 2;
        if (candidate >= 0 && detectFormattingCodeLength(text, candidate) == 2) {
            return candidate;
        }
        candidate = endExclusive - 1;
        return candidate >= 0 && detectFormattingCodeLength(text, candidate) == 1 ? candidate : -1;
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
                    i += length;
                    continue;
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
    public String stripColorCodes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder stripped = new StringBuilder(text.length());
        for (int i = 0; i < text.length();) {
            int length = detectFormattingCodeLength(text, i);
            if (length > 0) {
                i += length;
                continue;
            }
            stripped.append(text.charAt(i));
            i++;
        }
        return stripped.toString();
    }

    @Override
    public String getActiveFormatting(String s) {
        if (s == null) {
            return "";
        }
        String color = "";
        boolean k = false, l = false, m = false, n = false, o = false;
        for (int i = 0; i < s.length() - 1; i++) {
            if (s.charAt(i) == SECTION_SIGN) {
                char c = Character.toLowerCase(s.charAt(i + 1));
                if (c == 'r') {
                    color = "";
                    k = l = m = n = o = false;
                } else if (isFormatColor(c)) {
                    color = SECTION_SIGN + String.valueOf(c);
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
    public String getFormatFromString(String str) {
        if (str == null) {
            return "";
        }
        String result = "";
        int index = -1;
        int length = str.length();
        while ((index = str.indexOf(SECTION_SIGN, index + 1)) != -1) {
            if (index < length - 1) {
                char c0 = str.charAt(index + 1);
                if (isFormatColor(c0)) {
                    result = SECTION_SIGN + String.valueOf(c0);
                    continue;
                }
                if (isFormatSpecial(c0)) {
                    result = result + SECTION_SIGN + c0;
                }
            }
        }
        return result;
    }

    @Override
    public boolean isFormatSpecial(char c) {
        return (c >= 'k' && c <= 'o') || (c >= 'K' && c <= 'O') || c == 'r' || c == 'R';
    }

    @Override
    public boolean isFormatColor(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}
