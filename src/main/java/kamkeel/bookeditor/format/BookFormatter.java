package kamkeel.bookeditor.format;

/**
 * Strategy object that encapsulates colour and formatting handling for the book
 * editor. Implementations can provide either the vanilla Minecraft behaviour or
 * delegate to Hex Text when the mod is available.
 */
public abstract class BookFormatter {

    /**
     * Removes any colour and formatting codes from the supplied text.
     */
    public abstract String stripColorCodes(CharSequence text);

    /**
     * Detects the length of a formatting code that begins at {@code index}.
     * Returns {@code 0} when no code is present at that position.
     */
    public abstract int detectFormattingCodeLength(CharSequence text, int index);

    /**
     * Attempts to locate the start of a formatting code that terminates at
     * {@code endExclusive}. Returns {@code -1} when no code ends at that
     * position.
     */
    public abstract int findFormattingCodeStart(CharSequence text, int endExclusive);

    /**
     * Removes dangling formatting markers that could crash the client when the
     * book is sent to the server.
     */
    public abstract String sanitizeFormatting(String text);

    /**
     * Returns {@code true} when {@code code} represents a colour code.
     */
    public abstract boolean isFormatColor(char code);

    /**
     * Returns {@code true} when {@code code} represents a style/special code.
     */
    public abstract boolean isFormatSpecial(char code);

    /**
     * Returns the currently active formatting codes in {@code text}.
     */
    public abstract String getActiveFormatting(String text);

    /**
     * Extracts the formatting codes that should be applied to the start of
     * {@code text}.
     */
    public abstract String getFormatFromString(String text);
}
