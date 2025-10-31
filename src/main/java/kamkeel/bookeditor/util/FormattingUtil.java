package kamkeel.bookeditor.util;

import kamkeel.bookeditor.controller.BookController;
import kamkeel.bookeditor.format.BookFormatter;

/**
 * Utility methods for working with formatting codes. Delegates to the
 * {@link BookFormatter} selected by {@link BookController} so that additional
 * syntax provided by optional mods is respected.
 */
public final class FormattingUtil {
    private FormattingUtil() {
    }

    private static BookFormatter formatter() {
        return BookController.getFormatter();
    }

    public static int detectFormattingCodeLength(CharSequence text, int index) {
        return formatter().detectFormattingCodeLength(text, index);
    }

    public static int findFormattingCodeStart(CharSequence text, int endExclusive) {
        return formatter().findFormattingCodeStart(text, endExclusive);
    }

    public static String sanitizeFormatting(String text) {
        return formatter().sanitizeFormatting(text);
    }
}
