package kamkeel.bookeditor.format;

/**
 * Formatter that mirrors the vanilla Minecraft formatting rules used by the
 * original Book Editor implementation.
 */
public class StandaloneBookFormatter extends BookFormatter {
    @Override
    public String getDisplayName() {
        return "Standalone";
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
            char code = text.charAt(endExclusive - 1);
            if (isFormatColor(code) || isFormatSpecial(code)) {
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
    public String stripColorCodes(CharSequence text) {
        if (text == null) {
            return null;
        }
        return text.toString().replaceAll("\u00a7[0-9a-fA-Fk-oK-OrR]?", "");
    }

    @Override
    public String getActiveFormatting(CharSequence text) {
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
    public String getFormatFromString(CharSequence text) {
        if (text == null) {
            return "";
        }
        String result = "";
        int index = -1;
        int length = text.length();
        while ((index = indexOf(text, '\u00a7', index + 1)) != -1) {
            if (index < length - 1) {
                char code = text.charAt(index + 1);
                if (isFormatColor(code)) {
                    result = "\u00a7" + code;
                    continue;
                }
                if (isFormatSpecial(code)) {
                    result = result + "\u00a7" + code;
                }
            }
        }
        return result;
    }

    private int indexOf(CharSequence text, char target, int fromIndex) {
        for (int i = Math.max(0, fromIndex); i < text.length(); i++) {
            if (text.charAt(i) == target) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isFormatColor(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    @Override
    public boolean isFormatSpecial(char c) {
        return (c >= 'k' && c <= 'o') || (c >= 'K' && c <= 'O') || c == 'r' || c == 'R';
    }
}
