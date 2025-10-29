package kamkeel.bookeditor.format;

import kamkeel.hextext.common.util.ColorCodeUtils;
import kamkeel.hextext.common.util.StringUtils;

/**
 * Formatter that delegates to Hex Text's colour parsing utilities to gain RGB
 * and extended formatting support when the mod is present.
 */
public class HexTextBookFormatter extends StandaloneBookFormatter {

    @Override
    public String stripColorCodes(CharSequence text) {
        return text == null ? null : StringUtils.stripColorCodes(text);
    }

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
            int length = ColorCodeUtils.detectColorCodeLength(text, i);
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
            int length = ColorCodeUtils.detectColorCodeLength(text, i);
            if (length > 0) {
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
    public boolean isFormatColor(char code) {
        return ColorCodeUtils.isMinecraftColorCode(code) || Character.toLowerCase(code) == 'g';
    }

    @Override
    public boolean isFormatSpecial(char code) {
        return ColorCodeUtils.isStyleCode(code) || ColorCodeUtils.isEffectCode(code) || ColorCodeUtils.isResetCode(code);
    }

    @Override
    public String getActiveFormatting(String text) {
        String formatting = StringUtils.extractFormatFromString(text);
        return formatting != null ? formatting : "";
    }

    @Override
    public String getFormatFromString(String text) {
        String formatting = StringUtils.extractFormatFromString(text);
        return formatting != null ? formatting : "";
    }
}
