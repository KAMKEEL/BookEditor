package kamkeel.bookeditor.util;

import kamkeel.bookeditor.book.BookController;
import kamkeel.bookeditor.format.BookFormatter;

/**
 * Utility methods that delegate to the active {@link BookFormatter}. Keeping a
 * thin facade here avoids touching the majority of the existing call sites
 * while still enabling Hex Text integration.
 */
public final class FormattingUtil {
    private FormattingUtil() {
    }

    private static BookFormatter formatter() {
        return BookController.getFormatter();
    }

    public static String stripColorCodes(CharSequence text) {
        return formatter().stripColorCodes(text);
    }

    /**
     * Detects the length of a formatting code that starts at the provided index.
     * Returns {@code 0} when no code starts at the index.
     */
    public static int detectFormattingCodeLength(CharSequence text, int index) {
        return formatter().detectFormattingCodeLength(text, index);
    }

    /**
     * Attempts to find the starting index of a formatting code that ends at {@code endExclusive}.
     * Returns {@code -1} if no formatting code terminates at that position.
     */
    public static int findFormattingCodeStart(CharSequence text, int endExclusive) {
        return formatter().findFormattingCodeStart(text, endExclusive);
    }

    /**
     * Removes dangling formatting markers that would otherwise crash the client when sent to the server.
     */
    public static String sanitizeFormatting(String text) {
        return formatter().sanitizeFormatting(text);
    }
}
