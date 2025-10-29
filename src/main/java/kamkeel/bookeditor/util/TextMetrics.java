package kamkeel.bookeditor.util;

/**
 * Simple abstraction over the string measurement operations needed for
 * wrapping lines inside the book. In production a Minecraft backed
 * implementation is used, while tests can inject lightweight stubs.
 */
public interface TextMetrics {
    int stringWidth(String text);

    int charWidth(char character);

    int sizeStringToWidth(String text, int maxWidth);
}
