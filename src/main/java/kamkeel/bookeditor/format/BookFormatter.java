package kamkeel.bookeditor.format;

import kamkeel.bookeditor.util.TextMetrics;

/**
 * Abstraction responsible for handling the different formatting rules supported
 * by the book editor. Implementations adapt to either the vanilla formatting
 * or the Hex Text enhanced formatting depending on which mods are available at
 * runtime.
 */
public abstract class BookFormatter {
    private final TextMetrics defaultMetrics;
    private volatile TextMetrics metrics;

    protected BookFormatter(TextMetrics defaultMetrics) {
        if (defaultMetrics == null) {
            throw new IllegalArgumentException("defaultMetrics");
        }
        this.defaultMetrics = defaultMetrics;
        this.metrics = defaultMetrics;
    }

    public TextMetrics getTextMetrics() {
        return metrics;
    }

    public void setTextMetrics(TextMetrics metrics) {
        this.metrics = metrics != null ? metrics : defaultMetrics;
    }

    public void resetTextMetrics() {
        this.metrics = defaultMetrics;
    }

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

    public int findFormattingCodeStart(CharSequence text, int endExclusive) {
        if (text == null || endExclusive <= 0 || endExclusive > text.length()) {
            return -1;
        }
        for (int index = endExclusive - 1; index >= 0; index--) {
            int codeLength = detectFormattingCodeLength(text, index);
            if (codeLength > 0 && index + codeLength == endExclusive) {
                return index;
            }
        }
        return -1;
    }

    public String sanitizeFormatting(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sanitized = new StringBuilder(text.length());
        int i = 0;
        while (i < text.length()) {
            int length = detectFormattingCodeLength(text, i);
            if (length > 0) {
                if (length == 1 || i + length > text.length()) {
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

    public String stripColorCodes(CharSequence text) {
        if (text == null) {
            return null;
        }
        StringBuilder stripped = new StringBuilder(text.length());
        int i = 0;
        while (i < text.length()) {
            int length = detectFormattingCodeLength(text, i);
            if (length > 0) {
                if (length == 1) {
                    i++;
                } else {
                    i += length;
                }
                continue;
            }
            stripped.append(text.charAt(i));
            i++;
        }
        return stripped.toString();
    }

    public String getActiveFormatting(String s) {
        if (s == null) {
            return "";
        }
        String color = "";
        boolean k = false, l = false, m = false, n = false, o = false;
        int index = 0;
        while (index < s.length()) {
            int length = detectFormattingCodeLength(s, index);
            if (length <= 0) {
                index++;
                continue;
            }
            if (length < 2) {
                break;
            }
            char c = Character.toLowerCase(s.charAt(index + 1));
            if (c == 'r') {
                color = "";
                k = l = m = n = o = false;
            } else if (isFormatColor(c)) {
                color = s.substring(index, Math.min(index + length, s.length()));
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
            index += length;
        }
        StringBuilder out = new StringBuilder();
        if (!color.isEmpty()) {
            out.append(color);
        }
        if (k) out.append("§k");
        if (l) out.append("§l");
        if (m) out.append("§m");
        if (n) out.append("§n");
        if (o) out.append("§o");
        return out.toString();
    }

    public String getFormatFromString(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder active = new StringBuilder();
        int index = 0;
        while (index < str.length()) {
            int length = detectFormattingCodeLength(str, index);
            if (length <= 0) {
                index++;
                continue;
            }
            if (length < 2) {
                break;
            }
            char c0 = str.charAt(index + 1);
            if (isFormatColor(c0)) {
                active.setLength(0);
                active.append(str, index, Math.min(index + length, str.length()));
            } else if (isFormatSpecial(c0)) {
                active.append(str, index, Math.min(index + length, str.length()));
            }
            index += length;
        }
        return active.toString();
    }

    public boolean isFormatSpecial(char c) {
        c = Character.toLowerCase(c);
        return (c >= 'k' && c <= 'o') || c == 'r';
    }

    public boolean isFormatColor(char c) {
        c = Character.toLowerCase(c);
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
    }

    public abstract String getName();
}
