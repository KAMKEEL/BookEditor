package kamkeel.bookeditor.util;

/**
 * Deterministic {@link TextMetrics} implementation that approximates the
 * default book font. Primarily used in tests or when Minecraft's font renderer
 * is unavailable.
 */
public class SimpleTextMetrics implements TextMetrics {
    private final int charWidth;

    public SimpleTextMetrics() {
        this(6);
    }

    public SimpleTextMetrics(int charWidth) {
        this.charWidth = Math.max(1, charWidth);
    }

    @Override
    public int stringWidth(String text) {
        if (text == null) {
            return 0;
        }
        int width = 0;
        for (int i = 0; i < text.length();) {
            int codeLength = FormattingUtil.detectFormattingCodeLength(text, i);
            if (codeLength > 0) {
                i += codeLength;
                continue;
            }
            width += charWidth;
            i++;
        }
        return width;
    }

    @Override
    public int charWidth(char character) {
        if (character == '\u00a7') {
            return 0;
        }
        return charWidth;
    }

    @Override
    public int sizeStringToWidth(String text, int maxWidth) {
        if (text == null || maxWidth <= 0) {
            return 0;
        }

        int width = 0;
        int index = 0;
        while (index < text.length()) {
            int codeLength = FormattingUtil.detectFormattingCodeLength(text, index);
            if (codeLength > 0) {
                index += codeLength;
                continue;
            }

            width += charWidth;
            if (width > maxWidth) {
                break;
            }
            index++;
        }

        return index;
    }
}
