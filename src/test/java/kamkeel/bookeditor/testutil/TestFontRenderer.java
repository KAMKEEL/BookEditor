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
        boolean skipNext = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (skipNext) {
                skipNext = false;
                continue;
            }
            if (c == '\u00a7') {
                skipNext = true;
                continue;
            }
            width += getCharWidth(c);
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
        boolean skipNext = false;
        while (consumed < text.length()) {
            char c = text.charAt(consumed);
            if (skipNext) {
                skipNext = false;
                consumed++;
                continue;
            }
            if (c == '\u00a7') {
                skipNext = true;
                consumed++;
                continue;
            }
            int charWidth = getCharWidth(c);
            if (width + charWidth > maxWidth) {
                break;
            }
            width += charWidth;
            consumed++;
        }
        return consumed;
    }

    public int getDefaultWidth() {
        return defaultWidth;
    }
}
