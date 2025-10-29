package kamkeel.plugeditor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kamkeel.plugeditor.book.Line;
import net.minecraft.client.gui.FontRenderer;

/**
 * Vanilla-compatible implementation of {@link BookTextFormatter}. This mirrors
 * the behaviour the editor previously relied on when HexText is unavailable.
 */
public final class BookUtils implements BookTextFormatter {
    public static final BookUtils INSTANCE = new BookUtils();

    private BookUtils() {
    }

    @Override
    public List<String> listFormattedStringToWidth(FontRenderer renderer, String text, String wrappedFormatting, int maxWidth) {
        if (renderer == null) {
            return Collections.singletonList(text == null ? "" : text);
        }

        String prefix = wrappedFormatting == null ? "" : wrappedFormatting;
        String content = text == null ? "" : text;
        String wrapped = wrapStringToWidth(renderer, content, maxWidth, prefix);
        if (wrapped.isEmpty()) {
            return Collections.singletonList("");
        }
        return new ArrayList<String>(Arrays.asList(wrapped.split(String.valueOf(Line.SPLIT_CHAR), -1)));
    }

    @Override
    public int detectColorCodeLength(CharSequence text, int index) {
        if (text == null || index < 0 || index >= text.length()) {
            return 0;
        }
        char c = text.charAt(index);
        if ((c == '\u00a7' || c == '&') && index + 1 < text.length()) {
            char next = text.charAt(index + 1);
            if (isLegacyFormatColor(next) || isLegacyFormatSpecial(next)) {
                return 2;
            }
        }
        return 0;
    }

    @Override
    public int findColorCodeStart(CharSequence text, int endExclusive) {
        if (text == null || endExclusive <= 0 || endExclusive > text.length()) {
            return -1;
        }
        for (int i = endExclusive - 1; i >= 0; i--) {
            int length = detectColorCodeLength(text, i);
            if (length > 0 && i + length == endExclusive) {
                return i;
            }
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
            int codeLength = detectColorCodeLength(text, i);
            if (codeLength > 0) {
                if (i + codeLength > text.length()) {
                    break;
                }
                sanitized.append(text, i, i + codeLength);
                i += codeLength;
            } else {
                sanitized.append(text.charAt(i));
                i++;
            }
        }
        return sanitized.toString();
    }

    @Override
    public String extractActiveFormatting(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        String color = "";
        boolean k = false;
        boolean l = false;
        boolean m = false;
        boolean n = false;
        boolean o = false;

        for (int i = 0; i < s.length() - 1; i++) {
            if (s.charAt(i) == '\u00a7') {
                char c = Character.toLowerCase(s.charAt(i + 1));
                if (c == 'r') {
                    color = "";
                    k = l = m = n = o = false;
                } else if (isLegacyFormatColor(c)) {
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
    public String stripColorCodes(CharSequence text) {
        if (text == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            int length = detectColorCodeLength(text, i);
            if (length > 0) {
                i += length - 1;
                continue;
            }
            builder.append(text.charAt(i));
        }
        return builder.toString();
    }

    private String wrapStringToWidth(FontRenderer renderer, String text, int maxWidth, String wrappedFormatting) {
        if (text == null) {
            return "";
        }
        int maxCharsInWidth = sizeStringToWidth(renderer, wrappedFormatting + text, maxWidth) - wrappedFormatting.length();
        if (text.length() <= maxCharsInWidth) {
            return text;
        }

        if (maxCharsInWidth <= 0) {
            maxCharsInWidth = 1;
        }

        String firstSegment = text.substring(0, maxCharsInWidth);
        char splitChar = text.charAt(maxCharsInWidth);
        boolean newlineOrSpace = (splitChar == ' ' || splitChar == '\n');
        String remainder = text.substring(maxCharsInWidth + (newlineOrSpace ? 1 : 0));
        if (newlineOrSpace) {
            firstSegment = firstSegment + splitChar;
        }

        String activeFormatting = extractActiveFormatting(wrappedFormatting + firstSegment);
        String wrappedRemainder = remainder.isEmpty() ? "" : wrapStringToWidth(renderer, remainder, maxWidth, activeFormatting);
        if (wrappedRemainder.isEmpty()) {
            return firstSegment;
        }
        return firstSegment + Line.SPLIT_CHAR + wrappedRemainder;
    }

    private int sizeStringToWidth(FontRenderer renderer, String text, int maxWidth) {
        return FontRendererPrivate.callSizeStringToWidth(renderer, text, maxWidth);
    }

    private boolean isLegacyFormatColor(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private boolean isLegacyFormatSpecial(char c) {
        return (c >= 'k' && c <= 'o') || (c >= 'K' && c <= 'O') || c == 'r' || c == 'R';
    }
}
