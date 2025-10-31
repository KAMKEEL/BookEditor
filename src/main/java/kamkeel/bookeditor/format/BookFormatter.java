package kamkeel.bookeditor.format;

/**
 * Base abstraction for formatting-aware behaviour in the book editor. Concrete
 * implementations encapsulate how formatting codes are detected, normalised and
 * propagated when wrapping text across multiple lines.
 */
public abstract class BookFormatter {
    protected static final char SECTION_SIGN = '\u00a7';

    /**
     * Detects the length of the formatting token at {@code index}. Returns
     * {@code 0} when no formatting token starts at the index and {@code 1} when
     * the token is incomplete (e.g. a trailing section sign).
     */
    public abstract int detectFormattingCodeLength(CharSequence text, int index);

    /**
     * Finds the start index of a formatting token that ends at the supplied
     * position.
     */
    public abstract int findFormattingCodeStart(CharSequence text, int endExclusive);

    /**
     * Removes invalid or truncated formatting sequences that would otherwise
     * crash the vanilla client when sent to the server.
     */
    public abstract String sanitizeFormatting(String text);

    /**
     * Removes recognised formatting codes from the provided text.
     */
    public abstract String stripColorCodes(String text);

    /**
     * Determines the formatting that should remain active after the end of the
     * supplied text.
     */
    public abstract String getActiveFormatting(String s);

    /**
     * Extracts all formatting codes that appear inside the supplied text.
     */
    public abstract String getFormatFromString(String str);

    /**
     * Returns {@code true} when the supplied character represents a special
     * formatting code (bold, italic, underline, etc.).
     */
    public abstract boolean isFormatSpecial(char c);

    /**
     * Returns {@code true} when the supplied character represents a colour
     * formatting code.
     */
    public abstract boolean isFormatColor(char c);
}
