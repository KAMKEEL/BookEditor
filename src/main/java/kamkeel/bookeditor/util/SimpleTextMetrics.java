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
        return text == null ? 0 : text.length() * charWidth;
    }

    @Override
    public int charWidth(char character) {
        return charWidth;
    }

    @Override
    public int sizeStringToWidth(String text, int maxWidth) {
        if (text == null) {
            return 0;
        }
        if (maxWidth <= 0) {
            return 0;
        }
        int maxChars = maxWidth / charWidth;
        return Math.min(text.length(), Math.max(0, maxChars));
    }
}
