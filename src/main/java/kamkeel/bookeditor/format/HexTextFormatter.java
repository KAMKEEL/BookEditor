package kamkeel.bookeditor.format;

import kamkeel.hextext.common.util.ColorCodeUtils;
import kamkeel.hextext.common.util.StringUtils;

/**
 * Formatter that delegates to Hex Text's string utilities, allowing the book
 * editor to support extended RGB formatting when the mod is present.
 */
public class HexTextFormatter extends BookFormatter {

    @Override
    public int detectFormattingCodeLength(CharSequence text, int index) {
        if (text == null) {
            return 0;
        }
        return ColorCodeUtils.detectColorCodeLengthIgnoringRaw(text, index);
    }

    @Override
    public String sanitizeFormatting(String text) {
        // Hex Text's detection already returns 0 for incomplete sequences, so we
        // can rely on the default implementation for sanitising.
        return super.sanitizeFormatting(text);
    }

    @Override
    public String stripColorCodes(CharSequence text) {
        return StringUtils.stripColorCodes(text);
    }

    @Override
    public String getActiveFormatting(String text) {
        return StringUtils.extractFormatFromString(text);
    }

    @Override
    public String getFormatFromString(String text) {
        return StringUtils.extractFormatFromString(text);
    }

    @Override
    public boolean isFormatColor(char character) {
        return ColorCodeUtils.isMinecraftColorCode(character)
            || Character.toLowerCase(character) == 'g';
    }

    @Override
    public boolean isFormatSpecial(char character) {
        char lowered = Character.toLowerCase(character);
        return ColorCodeUtils.isStyleCode(lowered)
            || ColorCodeUtils.isEffectCode(lowered)
            || ColorCodeUtils.isResetCode(lowered);
    }
}
