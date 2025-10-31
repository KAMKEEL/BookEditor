package kamkeel.bookeditor.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;

/**
 * Formatter that understands the extended formatting syntax provided by the
 * HexText mod. The behaviour is guarded by suppliers so unit tests can toggle
 * features such as ampersand or HTML style formatting.
 */
public final class HexTextBookFormatter extends BookFormatter {
    private final BooleanSupplier allowAmpersand;
    private final BooleanSupplier allowHtml;

    public HexTextBookFormatter(BooleanSupplier allowAmpersand, BooleanSupplier allowHtml) {
        this.allowAmpersand = allowAmpersand != null ? allowAmpersand : () -> true;
        this.allowHtml = allowHtml != null ? allowHtml : () -> false;
    }

    private boolean allowAmpersand() {
        return allowAmpersand.getAsBoolean();
    }

    private boolean allowHtmlFormatting() {
        return allowHtml.getAsBoolean();
    }

    @Override
    public int detectFormattingCodeLength(CharSequence text, int index) {
        if (text == null || index < 0 || index >= text.length()) {
            return 0;
        }
        char marker = text.charAt(index);
        if (isMarker(marker)) {
            if (index + 1 >= text.length()) {
                return 1;
            }
            char code = text.charAt(index + 1);
            if (code == '#') {
                return hasHexDigits(text, index + 2, 6) ? 8 : 1;
            }
            if (isFormattingCode(code)) {
                return 2;
            }
            return 1;
        }
        if (allowHtmlFormatting() && marker == '<') {
            if (index + 8 <= text.length() && text.charAt(index + 7) == '>' && hasHexDigits(text, index + 1, 6)) {
                return 8;
            }
            if (index + 9 <= text.length()
                && text.charAt(index + 1) == '/'
                && text.charAt(index + 8) == '>'
                && hasHexDigits(text, index + 2, 6)) {
                return 9;
            }
            return 1;
        }
        return 0;
    }

    @Override
    public int findFormattingCodeStart(CharSequence text, int endExclusive) {
        if (text == null || endExclusive <= 0 || endExclusive > text.length()) {
            return -1;
        }
        int[] lengths = {9, 8, 2, 1};
        for (int length : lengths) {
            int start = endExclusive - length;
            if (start >= 0 && detectFormattingCodeLength(text, start) == length) {
                return start;
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
        if (s == null || s.isEmpty()) {
            return "";
        }
        String color = "";
        boolean obfuscated = false;
        boolean bold = false;
        boolean strikethrough = false;
        boolean underline = false;
        boolean italic = false;
        List<String> htmlStack = new ArrayList<>();
        for (int i = 0; i < s.length();) {
            int length = detectFormattingCodeLength(s, i);
            if (length <= 0) {
                i++;
                continue;
            }
            char marker = s.charAt(i);
            if (marker == '<' && allowHtmlFormatting()) {
                if (length == 8) {
                    String tag = normaliseHtmlOpen(s, i);
                    htmlStack.add(tag);
                } else if (length == 9 && !htmlStack.isEmpty()) {
                    htmlStack.remove(htmlStack.size() - 1);
                }
                i += length;
                continue;
            }
            if (length == 8 && marker != '<' && s.charAt(i + 1) == '#') {
                color = normaliseHexColor(s, i);
                htmlStack.clear();
                obfuscated = bold = strikethrough = underline = italic = false;
                i += length;
                continue;
            }
            if (length >= 2 && isMarker(marker)) {
                char code = Character.toLowerCase(s.charAt(i + 1));
                if (code == 'r') {
                    color = "";
                    htmlStack.clear();
                    obfuscated = bold = strikethrough = underline = italic = false;
                } else if (isStandardColor(code) || isEffectCode(code) || code == '#') {
                    color = SECTION_SIGN + String.valueOf(code);
                    htmlStack.clear();
                    obfuscated = bold = strikethrough = underline = italic = false;
                } else if (code == 'k') {
                    obfuscated = true;
                } else if (code == 'l') {
                    bold = true;
                } else if (code == 'm') {
                    strikethrough = true;
                } else if (code == 'n') {
                    underline = true;
                } else if (code == 'o') {
                    italic = true;
                }
                i += length;
                continue;
            }
            i += length;
        }
        StringBuilder builder = new StringBuilder();
        if (!color.isEmpty()) {
            builder.append(color);
        }
        if (!htmlStack.isEmpty()) {
            for (String tag : htmlStack) {
                builder.append(tag);
            }
        }
        if (obfuscated) builder.append("§k");
        if (bold) builder.append("§l");
        if (strikethrough) builder.append("§m");
        if (underline) builder.append("§n");
        if (italic) builder.append("§o");
        return builder.toString();
    }

    @Override
    public String getFormatFromString(String str) {
        return getActiveFormatting(str);
    }

    @Override
    public boolean isFormatSpecial(char c) {
        char lower = Character.toLowerCase(c);
        return lower == 'r' || (lower >= 'k' && lower <= 'o');
    }

    @Override
    public boolean isFormatColor(char c) {
        char lower = Character.toLowerCase(c);
        return lower == '#' || isStandardColor(lower) || isEffectCode(lower);
    }

    private boolean isMarker(char marker) {
        return marker == SECTION_SIGN || (marker == '&' && allowAmpersand());
    }

    private static boolean hasHexDigits(CharSequence text, int start, int count) {
        if (text == null || start < 0 || start + count > text.length()) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            if (!isHexDigit(text.charAt(start + i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static boolean isFormattingCode(char c) {
        char lower = Character.toLowerCase(c);
        return isStandardColor(lower) || isStyleCode(lower) || isEffectCode(lower) || lower == 'r' || lower == '#';
    }

    private static boolean isStandardColor(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
    }

    private static boolean isStyleCode(char c) {
        return c >= 'k' && c <= 'o';
    }

    private static boolean isEffectCode(char c) {
        return c >= 'g' && c <= 'j';
    }

    private static String normaliseHexColor(CharSequence text, int index) {
        String hex = text.subSequence(index + 2, index + 8).toString().toLowerCase(Locale.ROOT);
        return "§#" + hex;
    }

    private static String normaliseHtmlOpen(CharSequence text, int index) {
        return text.subSequence(index, index + 8).toString();
    }
}
