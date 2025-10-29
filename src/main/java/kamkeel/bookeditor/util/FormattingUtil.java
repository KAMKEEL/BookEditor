package kamkeel.bookeditor.util;

import kamkeel.bookeditor.book.BookController;

/**
 * Utility methods for working with formatting codes. The heavy lifting is
 * delegated to the active {@link kamkeel.bookeditor.format.BookFormatter},
 * allowing runtime selection between the standalone and Hex Text backed
 * implementations.
 */
public final class FormattingUtil {
    private FormattingUtil() {
    }

    public static int detectFormattingCodeLength(CharSequence text, int index) {
        return BookController.getFormatter().detectFormattingCodeLength(text, index);
    }

    public static int findFormattingCodeStart(CharSequence text, int endExclusive) {
        return BookController.getFormatter().findFormattingCodeStart(text, endExclusive);
    }

    public static String sanitizeFormatting(String text) {
        return BookController.getFormatter().sanitizeFormatting(text);
    }

    public static String stripColorCodes(CharSequence text) {
        return BookController.getFormatter().stripColorCodes(text);
    }
}
