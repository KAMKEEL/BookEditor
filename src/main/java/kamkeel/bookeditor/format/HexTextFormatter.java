package kamkeel.bookeditor.format;

import kamkeel.bookeditor.util.MinecraftTextMetrics;
import kamkeel.hextext.common.util.ColorCodeUtils;
import kamkeel.hextext.common.util.StringUtils;

/**
 * Formatter implementation that leverages Hex Text utilities when the mod is
 * present. This enables support for Hex Text specific colour and formatting
 * sequences while keeping compatibility with the base game behaviour.
 */
public class HexTextFormatter extends BookFormatter {
    public HexTextFormatter() {
        super(new MinecraftTextMetrics());
    }

    @Override
    public int detectFormattingCodeLength(CharSequence text, int index) {
        int length = ColorCodeUtils.detectColorCodeLengthIgnoringRaw(text, index);
        return length > 0 ? length : super.detectFormattingCodeLength(text, index);
    }

    @Override
    public String stripColorCodes(CharSequence text) {
        if (text == null) {
            return null;
        }
        return StringUtils.stripColorCodes(text);
    }

    @Override
    public String getActiveFormatting(String s) {
        if (s == null) {
            return "";
        }
        String format = StringUtils.extractFormatFromString(s);
        return format != null ? format : "";
    }

    @Override
    public String getFormatFromString(String str) {
        if (str == null) {
            return "";
        }
        String format = StringUtils.extractFormatFromString(str);
        return format != null ? format : "";
    }

    @Override
    public boolean isFormatSpecial(char c) {
        return ColorCodeUtils.isStyleCode(c) || ColorCodeUtils.isEffectCode(c) || ColorCodeUtils.isResetCode(c);
    }

    @Override
    public boolean isFormatColor(char c) {
        return ColorCodeUtils.isMinecraftColorCode(c) || Character.toLowerCase(c) == 'g';
    }

    @Override
    public String getName() {
        return "HexTextFormatter";
    }
}
