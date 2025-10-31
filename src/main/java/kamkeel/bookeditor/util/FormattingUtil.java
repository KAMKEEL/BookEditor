package kamkeel.bookeditor.util;

import kamkeel.bookeditor.BookController;

/**
 * Thin wrapper around the active {@link kamkeel.bookeditor.format.BookFormatter} implementation.
 * The controller decides which formatter to expose depending on the environment and optional
 * HexText integration.
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

    public static String stripColorCodes(String text) {
        return BookController.getFormatter().stripColorCodes(text);
    }
}
