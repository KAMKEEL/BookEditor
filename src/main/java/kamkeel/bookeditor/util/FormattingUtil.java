package kamkeel.bookeditor.util;

import kamkeel.bookeditor.BookController;

/**
 * Utility facade that delegates formatting operations to the active
 * {@link BookFormatter}. Keeping this class means the rest of the codebase does
 * not need to be rewritten to understand formatter selection.
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
}
