package kamkeel.bookeditor.format;

/**
 * Abstraction for formatting engines that understand Minecraft style control
 * codes. Implementations can provide support for vanilla formatting or
 * integrations with other mods (such as HexText).
 */
public abstract class BookFormatter {

    /**
     * @return a human readable label describing the formatter implementation.
     */
    public abstract String getDisplayName();

    /**
     * Detects the length of a formatting code starting at the supplied index.
     * A return value of {@code 0} indicates there is no formatting sequence at
     * that position.
     */
    public abstract int detectFormattingCodeLength(CharSequence text, int index);

    /**
     * Attempts to locate the starting index of a formatting sequence whose end
     * corresponds to {@code endExclusive}.
     */
    public abstract int findFormattingCodeStart(CharSequence text, int endExclusive);

    /**
     * Removes dangling or incomplete formatting markers from the provided
     * string.
     */
    public abstract String sanitizeFormatting(String text);

    /**
     * Removes formatting information from the provided input leaving behind the
     * visible characters only.
     */
    public abstract String stripColorCodes(CharSequence text);

    /**
     * Determines the active formatting codes at the end of the provided text.
     */
    public abstract String getActiveFormatting(CharSequence text);

    /**
     * Extracts all formatting codes present in the string, preserving their
     * order of appearance.
     */
    public abstract String getFormatFromString(CharSequence text);

    /**
     * Returns {@code true} if the provided character represents a colour code.
     */
    public abstract boolean isFormatColor(char c);

    /**
     * Returns {@code true} if the provided character represents a special
     * (style/effect) formatting code.
     */
    public abstract boolean isFormatSpecial(char c);
}
