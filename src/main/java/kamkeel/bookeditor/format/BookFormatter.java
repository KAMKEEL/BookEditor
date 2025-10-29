package kamkeel.bookeditor.format;

/**
 * Base abstraction that encapsulates Minecraft text formatting behaviour.
 * Implementations can provide different parsing logic depending on whether
 * optional mods such as Hex Text are present.
 */
public abstract class BookFormatter {

    /**
     * Detects the length of a formatting code that starts at {@code index}.
     * Returns {@code 0} when no formatting marker begins at the index.
     */
    public abstract int detectFormattingCodeLength(CharSequence text, int index);

    /**
     * Finds the starting index of a formatting code that ends at
     * {@code endExclusive}. Returns {@code -1} if the position is not the end
     * of a formatting code.
     */
    public int findFormattingCodeStart(CharSequence text, int endExclusive) {
        if (text == null || endExclusive <= 0 || endExclusive > text.length()) {
            return -1;
        }
        int i = Math.min(endExclusive, text.length());
        while (i-- > 0) {
            int length = detectFormattingCodeLength(text, i);
            if (length <= 0) {
                continue;
            }
            if (i + length == endExclusive) {
                return i;
            }
            if (i + length < endExclusive) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Removes dangling formatting markers that would otherwise break the
     * rendered text. Partial sequences are dropped entirely.
     */
    public String sanitizeFormatting(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sanitized = new StringBuilder(text.length());
        int index = 0;
        while (index < text.length()) {
            int length = detectFormattingCodeLength(text, index);
            if (length > 0) {
                if (!isCompleteFormattingCode(text, index, length)) {
                    break;
                }
                sanitized.append(text, index, index + length);
                index += length;
                continue;
            }
            sanitized.append(text.charAt(index));
            index++;
        }
        return sanitized.toString();
    }

    /**
     * Strips all formatting codes from the provided text.
     */
    public String stripColorCodes(CharSequence text) {
        if (text == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(text.length());
        int index = 0;
        while (index < text.length()) {
            int length = detectFormattingCodeLength(text, index);
            if (length > 0 && isCompleteFormattingCode(text, index, length)) {
                index += length;
                continue;
            }
            builder.append(text.charAt(index));
            index++;
        }
        return builder.toString();
    }

    /**
     * Returns the currently active formatting sequence at the end of the
     * supplied text.
     */
    public abstract String getActiveFormatting(String text);

    /**
     * Extracts the formatting sequence embedded in the provided string.
     */
    public abstract String getFormatFromString(String text);

    /**
     * Determines whether the provided character is treated as a colour code.
     */
    public abstract boolean isFormatColor(char character);

    /**
     * Determines whether the provided character represents a style/effect code.
     */
    public abstract boolean isFormatSpecial(char character);

    /**
     * Hook for subclasses to signal whether a detected sequence is complete.
     */
    protected boolean isCompleteFormattingCode(CharSequence text, int index, int detectedLength) {
        return detectedLength > 0 && text != null && index + detectedLength <= text.length();
    }
}
