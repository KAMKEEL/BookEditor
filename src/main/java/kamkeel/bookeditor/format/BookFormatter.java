package kamkeel.bookeditor.format;

/**
 * Abstraction for the logic required to interpret and manipulate Minecraft
 * style formatting codes. Implementations can provide vanilla-compatible
 * behaviour or integrate optional mods that extend the formatting syntax.
 */
public abstract class BookFormatter {

    /**
     * Returns the length of a formatting sequence that starts at {@code index}.
     * When no formatting sequence begins at the position {@code 0} is returned.
     */
    public abstract int detectFormattingCodeLength(CharSequence text, int index);

    /**
     * Returns the index where a formatting sequence that terminates at
     * {@code endExclusive} begins, or {@code -1} if none could be found.
     */
    public abstract int findFormattingCodeStart(CharSequence text, int endExclusive);

    /**
     * Removes every formatting sequence from {@code text}.
     */
    public abstract String stripColorCodes(CharSequence text);

    /**
     * Returns the currently active formatting for {@code text}, including
     * colour and style codes that should be preserved when continuing input.
     */
    public abstract String getActiveFormatting(String text);

    /**
     * Extracts the trailing formatting tokens from {@code text} so that the
     * caller can prepend them to subsequent content.
     */
    public abstract String getFormatFromString(String text);

    /**
     * Returns {@code true} when {@code code} represents a colour code in the
     * active formatting syntax.
     */
    public abstract boolean isFormatColor(char code);

    /**
     * Returns {@code true} when {@code code} represents a style/reset code in
     * the active formatting syntax.
     */
    public abstract boolean isFormatSpecial(char code);

    /**
     * Sanitises {@code text} by removing dangling formatting markers that could
     * otherwise crash the client when sent to the server.
     */
    public String sanitizeFormatting(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sanitized = new StringBuilder(text.length());
        for (int i = 0; i < text.length();) {
            int length = detectFormattingCodeLength(text, i);
            if (length > 0) {
                if (i + length > text.length()) {
                    break;
                }
                sanitized.append(text, i, i + length);
                i += length;
                continue;
            }
            sanitized.append(text.charAt(i));
            i++;
        }
        return sanitized.toString();
    }
}
