package kamkeel.bookeditor.format;

/**
 * Strategy interface encapsulating formatting behaviour for book content.
 * Implementations can provide different parsing rules depending on whether
 * optional mods such as HexText are available.
 */
public abstract class BookFormatter {

    /**
     * Determines the length of a formatting code that begins at {@code index}.
     * Returns {@code 0} if no formatting code starts at that position.
     */
    public abstract int detectFormattingCodeLength(CharSequence text, int index);

    /**
     * Finds the starting index of a formatting code that terminates at
     * {@code endExclusive}. Returns {@code -1} when no code ends there.
     */
    public abstract int findFormattingCodeStart(CharSequence text, int endExclusive);

    /**
     * Removes dangling or partial formatting markers that could otherwise
     * crash the client when sent to the server.
     */
    public abstract String sanitizeFormatting(String text);

    /**
     * Returns the provided text with all colour/style markers stripped.
     */
    public abstract String stripColorCodes(CharSequence text);

    /**
     * Calculates the currently active formatting sequence contained within the
     * provided text.
     */
    public abstract String getActiveFormatting(String text);

    /**
     * Extracts the formatting codes present in {@code text} in a deterministic
     * order, mirroring Minecraft's behaviour.
     */
    public abstract String getFormatFromString(String text);

    /**
     * Returns {@code true} when {@code c} represents a colour code.
     */
    public abstract boolean isFormatColor(char c);

    /**
     * Returns {@code true} when {@code c} represents a style/special code
     * rather than a colour code.
     */
    public abstract boolean isFormatSpecial(char c);
}
