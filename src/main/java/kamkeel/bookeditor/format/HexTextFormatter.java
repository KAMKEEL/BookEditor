package kamkeel.bookeditor.format;

import kamkeel.hextext.common.util.ColorCodeUtils;
import kamkeel.hextext.config.HexTextConfig;

import java.util.function.BooleanSupplier;

/**
 * Formatter that mirrors key HexText behaviour without requiring the entire
 * mod to be initialised. The implementation understands HexText's extended
 * colour syntax (hex colours, ampersand aliases and HTML style tags) while
 * still falling back to vanilla behaviour when the optional mod is missing.
 */
public class HexTextFormatter extends BookFormatter {
    private static final char SECTION = '\u00a7';

    private final BooleanSupplier ampersandAllowedSupplier;
    private final BooleanSupplier htmlFormattingSupplier;

    public HexTextFormatter() {
        this(HexTextConfig::isAmpersandAllowed, HexTextConfig::isRgbHtmlFormatEnabled);
    }

    public HexTextFormatter(BooleanSupplier ampersandAllowedSupplier) {
        this(ampersandAllowedSupplier, HexTextConfig::isRgbHtmlFormatEnabled);
    }

    public HexTextFormatter(BooleanSupplier ampersandAllowedSupplier, BooleanSupplier htmlFormattingSupplier) {
        this.ampersandAllowedSupplier = ampersandAllowedSupplier != null ? ampersandAllowedSupplier : () -> false;
        this.htmlFormattingSupplier = htmlFormattingSupplier != null ? htmlFormattingSupplier : () -> false;
    }

    private boolean isAmpersandAllowed() {
        try {
            return ampersandAllowedSupplier.getAsBoolean();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean isHtmlFormattingAllowed() {
        try {
            return htmlFormattingSupplier.getAsBoolean();
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public int detectFormattingCodeLength(CharSequence text, int index) {
        if (text == null || index < 0 || index >= text.length()) {
            return 0;
        }
        char marker = text.charAt(index);
        if (marker == SECTION) {
            return detectPrefixedFormattingLength(text, index);
        }
        if (marker == '&') {
            return isAmpersandAllowed() ? detectPrefixedFormattingLength(text, index) : 0;
        }
        if (marker == '<' && isHtmlFormattingAllowed()) {
            return detectHtmlFormattingLength(text, index);
        }
        return 0;
    }

    @Override
    public int findFormattingCodeStart(CharSequence text, int endExclusive) {
        if (text == null || endExclusive <= 0 || endExclusive > text.length()) {
            return -1;
        }
        for (int i = endExclusive - 1; i >= 0; i--) {
            int length = detectFormattingCodeLength(text, i);
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
            int length = detectFormattingCodeLength(text, i);
            if (length > 0) {
                if (length < 2 && text.charAt(i) != '<') {
                    break;
                }
                if (i + length > text.length()) {
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
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length();) {
            int length = detectFormattingCodeLength(text, i);
            if (length > 0) {
                i += length;
                continue;
            }
            builder.append(text.charAt(i));
            i++;
        }
        return builder.toString();
    }

    @Override
    public String getActiveFormatting(String text) {
        if (text == null) {
            return "";
        }
        String color = "";
        StringBuilder styles = new StringBuilder();
        for (int i = 0; i < text.length();) {
            char marker = text.charAt(i);
            if (isFormattingMarker(marker)) {
                int length = detectPrefixedFormattingLength(text, i);
                if (length <= 0) {
                    i++;
                    continue;
                }
                if (length >= 2) {
                    char codeChar = Character.toLowerCase(text.charAt(i + 1));
                    if (codeChar == 'r') {
                        color = "";
                        styles.setLength(0);
                    } else if (codeChar == '#') {
                        if (length >= 8) {
                            color = canonicalPrefix(marker) + "#" + text.subSequence(i + 2, i + length);
                            styles.setLength(0);
                        }
                    } else if (ColorCodeUtils.isMinecraftColorCode(codeChar)) {
                        color = canonicalPrefix(marker) + codeChar;
                        styles.setLength(0);
                    } else if (ColorCodeUtils.isStyleCode(codeChar) || ColorCodeUtils.isEffectCode(codeChar)) {
                        String styleCode = canonicalPrefix(marker) + codeChar;
                        if (styles.indexOf(styleCode) < 0) {
                            styles.append(styleCode);
                        }
                    }
                }
                i += length;
                continue;
            }
            if (marker == '<' && isHtmlFormattingAllowed()) {
                int length = detectHtmlFormattingLength(text, i);
                if (length == 0) {
                    i++;
                    continue;
                }
                if (length == 9 && text.charAt(i + 1) == '/') {
                    color = "";
                    styles.setLength(0);
                } else if (length == 8) {
                    color = canonicalPrefix(SECTION) + "#" + text.subSequence(i + 1, i + 7);
                    styles.setLength(0);
                }
                i += length;
                continue;
            }
            i++;
        }
        return color + styles;
    }

    @Override
    public String getFormatFromString(String text) {
        return getActiveFormatting(text);
    }

    @Override
    public boolean isFormatColor(char c) {
        return c == '#' || ColorCodeUtils.isMinecraftColorCode(c);
    }

    @Override
    public boolean isFormatSpecial(char c) {
        return ColorCodeUtils.isStyleCode(c)
            || ColorCodeUtils.isEffectCode(c)
            || ColorCodeUtils.isResetCode(c);
    }

    private boolean isFormattingMarker(char marker) {
        return marker == SECTION || (marker == '&' && isAmpersandAllowed());
    }

    private String canonicalPrefix(char marker) {
        return String.valueOf(SECTION);
    }

    private int detectPrefixedFormattingLength(CharSequence text, int index) {
        int length = text.length();
        if (index + 1 >= length) {
            return 1;
        }
        char code = text.charAt(index + 1);
        if (code == '#') {
            int hexStart = index + 2;
            if (hexStart + 6 <= length && ColorCodeUtils.isValidHexString(text, hexStart)) {
                return 8;
            }
            return 1;
        }
        if (ColorCodeUtils.isFormattingCode(code)) {
            return 2;
        }
        return 0;
    }

    private int detectHtmlFormattingLength(CharSequence text, int index) {
        int length = text.length();
        if (index + 2 >= length) {
            return 0;
        }
        if (text.charAt(index + 1) == '/') {
            if (index + 9 <= length && text.charAt(index + 8) == '>'
                && ColorCodeUtils.isValidHexString(text, index + 2)) {
                return 9;
            }
            return 0;
        }
        if (index + 8 <= length && text.charAt(index + 7) == '>'
            && ColorCodeUtils.isValidHexString(text, index + 1)) {
            return 8;
        }
        return 0;
    }
}
