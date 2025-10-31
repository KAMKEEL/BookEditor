package kamkeel.bookeditor.format;

import kamkeel.hextext.CommonProxy;
import kamkeel.hextext.HexText;
import kamkeel.hextext.common.util.ColorCodeUtils;
import kamkeel.hextext.common.util.StringUtils;
import kamkeel.hextext.config.HexTextConfig;

/**
 * Formatter that delegates to HexText's rich formatting utilities when the mod
 * is present. The formatter gracefully falls back to sane defaults if the
 * HexText proxy has not been initialised yet (for example, during tests).
 */
public class HexTextBookFormatter extends BookFormatter {
    private static final int MAX_FORMATTING_LENGTH = 9; // Accounts for HTML style tags.

    public HexTextBookFormatter() {
        if (HexText.getActiveProxy() == null) {
            HexText.proxy = new CommonProxy();
        }
    }

    @Override
    public String getDisplayName() {
        return "HexText";
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
        int maxLookBack = Math.min(endExclusive, MAX_FORMATTING_LENGTH);
        for (int offset = 1; offset <= maxLookBack; offset++) {
            int candidate = endExclusive - offset;
            int length = detectFormattingCodeLength(text, candidate);
            if (length > 0 && candidate + length == endExclusive) {
                return candidate;
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
                sanitized.append(text, i, i + length);
                i += length;
                continue;
            }
            char current = text.charAt(i);
            if (isPotentialFormattingStart(current)) {
                break;
            }
            sanitized.append(current);
            i++;
        }
        return sanitized.toString();
    }

    @Override
    public String stripColorCodes(CharSequence text) {
        if (text == null) {
            return null;
        }
        return StringUtils.stripColorCodes(text);
    }

    @Override
    public String getActiveFormatting(CharSequence text) {
        if (text == null) {
            return "";
        }
        return StringUtils.extractFormatFromString(text.toString());
    }

    @Override
    public String getFormatFromString(CharSequence text) {
        if (text == null) {
            return "";
        }
        return StringUtils.extractFormatFromString(text.toString());
    }

    @Override
    public boolean isFormatColor(char c) {
        char lower = Character.toLowerCase(c);
        return ColorCodeUtils.isMinecraftColorCode(lower) || lower == '#';
    }

    @Override
    public boolean isFormatSpecial(char c) {
        char lower = Character.toLowerCase(c);
        return ColorCodeUtils.isStyleCode(lower) || ColorCodeUtils.isEffectCode(lower) || ColorCodeUtils.isResetCode(lower);
    }

    private boolean isPotentialFormattingStart(char c) {
        if (c == '\u00a7') {
            return true;
        }
        if (c == '&') {
            return HexTextConfig.isAmpersandAllowed();
        }
        if (c == '<') {
            return HexTextConfig.isRgbHtmlFormatEnabled();
        }
        return false;
    }
}
