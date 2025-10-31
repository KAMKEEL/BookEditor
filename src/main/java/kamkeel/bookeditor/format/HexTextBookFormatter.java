package kamkeel.bookeditor.format;

import kamkeel.hextext.HexText;
import kamkeel.hextext.common.util.ColorCodeUtils;
import kamkeel.hextext.common.util.StringUtils;

/**
 * Formatter that integrates with the Hex Text mod so that extended colour and
 * style codes are recognised by the book editor.
 */
public final class HexTextBookFormatter extends BookFormatter {

    @Override
    public int detectFormattingCodeLength(CharSequence text, int index) {
        return ColorCodeUtils.detectColorCodeLength(text, index);
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
    public String stripColorCodes(CharSequence text) {
        return StringUtils.stripColorCodes(text);
    }

    @Override
    public String getActiveFormatting(String text) {
        String result = StringUtils.extractFormatFromString(text);
        return result == null ? "" : result;
    }

    @Override
    public String getFormatFromString(String text) {
        String result = StringUtils.extractFormatFromString(text);
        return result == null ? "" : result;
    }

    @Override
    public boolean isFormatColor(char code) {
        if (ColorCodeUtils.isMinecraftColorCode(code)) {
            return true;
        }
        return code == '#';
    }

    @Override
    public boolean isFormatSpecial(char code) {
        return ColorCodeUtils.isStyleCode(code)
            || ColorCodeUtils.isEffectCode(code)
            || ColorCodeUtils.isResetCode(code);
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
                if (i + length > text.length()) {
                    break;
                }
                sanitized.append(text, i, i + length);
                i += length;
                continue;
            }
            char current = text.charAt(i);
            if (current == '\u00a7') {
                i++;
                continue;
            }
            if (current == '<' && i + 1 < text.length() && text.charAt(i + 1) == '#') {
                i++;
                continue;
            }
            sanitized.append(current);
            i++;
        }
        return sanitized.toString();
    }

    /**
     * Ensures the Hex Text proxy is present so that configuration lookups used
     * by {@link ColorCodeUtils} behave consistently during testing.
     */
    public static void ensureProxy() {
        if (HexText.proxy == null) {
            HexText.proxy = new kamkeel.hextext.CommonProxy();
        }
    }
}
