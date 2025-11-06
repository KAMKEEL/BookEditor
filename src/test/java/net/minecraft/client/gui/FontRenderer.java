package net.minecraft.client.gui;

/**
 * Minimal stub of Minecraft's FontRenderer for unit tests. The implementation
 * intentionally mirrors the behaviors relied upon by the Book Editor data model
 * so the logic can be exercised without a real Minecraft client.
 */
public class FontRenderer {
    private final int defaultCharWidth;

    public FontRenderer() {
        this(6);
    }

    public FontRenderer(int defaultCharWidth) {
        this.defaultCharWidth = defaultCharWidth;
    }

    /**
     * Returns the pixel width of the supplied character. Tests treat all
     * printable characters as fixed width except for Minecraft formatting
     * prefixes which are widthless.
     */
    public int getCharWidth(char c) {
        return isFormattingCode(c) ? 0 : defaultCharWidth;
    }

    /**
     * Returns the total pixel width of the string, ignoring formatting control
     * codes ("§").
     */
    public int getStringWidth(String text) {
        int width = 0;
        int index = 0;
        while (index < text.length()) {
            char c = text.charAt(index);
            if (c == '\u00a7') {
                index += skipFormatting(text, index);
                continue;
            }
            if (c == '\n') {
                index++;
                continue;
            }
            width += getCharWidth(c);
            index++;
        }
        return width;
    }

    /**
     * Core helper shared by both dev (sizeStringToWidth) and SRG
     * (func_78259_e) method names.
     */
    private int sizeStringToWidthInternal(String text, int maxWidth) {
        int width = 0;
        int index = 0;
        int lastSpaceConsumed = -1;
        while (index < text.length()) {
            char c = text.charAt(index);
            if (c == '\u00a7') {
                index += skipFormatting(text, index);
                continue;
            }
            if (c == '\n') {
                break;
            }
            int charWidth = getCharWidth(c);
            if (width + charWidth > maxWidth) {
                if (lastSpaceConsumed >= 0) {
                    return lastSpaceConsumed;
                }
                break;
            }
            width += charWidth;
            index++;
            if (c == ' ') {
                lastSpaceConsumed = index;
            }
        }
        return index;
    }

    /**
     * MCP-named accessor. Minecraft 1.7.10 exposes the same logic under two
     * different names depending on the environment; production code now calls
     * the SRG method directly but tests still expose both for completeness.
     */
    public int sizeStringToWidth(String text, int maxWidth) {
        return sizeStringToWidthInternal(text, maxWidth);
    }

    /**
     * SRG-named accessor. Delegates to the same implementation as
     * sizeStringToWidth so callers can rely on either name.
     */
    public int func_78259_e(String text, int maxWidth) {
        return sizeStringToWidthInternal(text, maxWidth);
    }

    private static boolean isFormattingCode(char c) {
        return c == '\n' || c == '\r' || c == '\t';
    }

    private int skipFormatting(String text, int index) {
        if (index + 1 >= text.length()) {
            return 1;
        }
        char next = text.charAt(index + 1);
        if (next == '#') {
            return Math.min(8, text.length() - index);
        }
        return 2;
    }
}
