package kamkeel.bookeditor.testutil;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.FontRenderer;

/**
 * Simple configurable FontRenderer used in unit tests. Each printable character
 * may be assigned a custom width to emulate the proportional font behaviour of
 * Minecraft's renderer while keeping the implementation deterministic.
 */
public class TestFontRenderer extends FontRenderer {
    private final Map<Character, Integer> overrides = new HashMap<Character, Integer>();
    private final int defaultWidth;

    public TestFontRenderer() {
        this(6);
    }

    public TestFontRenderer(int defaultWidth) {
        super(defaultWidth);
        this.defaultWidth = defaultWidth;
    }

    public void setWidth(char c, int width) {
        overrides.put(c, width);
    }

    @Override
    public int getCharWidth(char c) {
        Integer width = overrides.get(c);
        if (width != null) {
            return width;
        }
        return super.getCharWidth(c);
    }

    @Override
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

    @Override
    public int func_78259_e(String text, int maxWidth) {
        return sizeStringToWidth(text, maxWidth);
    }

    @Override
    public int sizeStringToWidth(String text, int maxWidth) {
        int consumed = 0;
        int width = 0;
        int lastSpaceConsumed = -1;
        while (consumed < text.length()) {
            char c = text.charAt(consumed);
            if (c == '\u00a7') {
                consumed += skipFormatting(text, consumed);
                continue;
            }
            if (c == '\n') {
                break;
            }
            int charWidth = getCharWidth(c);
            if (width + charWidth > maxWidth) {
                if (lastSpaceConsumed >= 0) {
                    consumed = lastSpaceConsumed;
                }
                break;
            }
            width += charWidth;
            consumed++;
            if (c == ' ') {
                lastSpaceConsumed = consumed;
            }
        }
        return consumed;
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

    public int getDefaultWidth() {
        return defaultWidth;
    }
}
