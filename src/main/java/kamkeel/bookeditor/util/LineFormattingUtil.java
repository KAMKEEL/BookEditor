package kamkeel.bookeditor.util;

import kamkeel.bookeditor.book.BookController;
import kamkeel.bookeditor.format.BookFormatter;

import java.util.Arrays;
import java.util.List;

/**
 * Collection of helpers related to measuring and wrapping line content.
 * The heavy lifting used to live inside {@code Line}; extracting it keeps the
 * data object lightweight and makes testing easier.
 */
public final class LineFormattingUtil {
    public static final int BOOK_TEXT_WIDTH = 116;

    private static volatile TextMetrics metrics = new MinecraftTextMetrics();

    private LineFormattingUtil() {
    }

    public static void setMetrics(TextMetrics newMetrics) {
        metrics = newMetrics != null ? newMetrics : new MinecraftTextMetrics();
    }

    public static TextMetrics getMetrics() {
        return metrics;
    }

    public static int sizeStringToApproxWidthBlind(String str, int lenPixels) {
        if (str == null) {
            return 0;
        }
        if (metrics.stringWidth(str) <= lenPixels) {
            return str.length();
        }
        int endIndex = sizeStringToWidthBlind(str, lenPixels);
        if (endIndex >= str.length()) {
            return str.length();
        }
        int partial = lenPixels - metrics.stringWidth(str.substring(0, endIndex));
        if (endIndex < str.length()) {
            int charWidth = Math.max(metrics.charWidth(str.charAt(endIndex)), 1);
            if ((float) partial / charWidth > 0.5f) {
                return endIndex + 1;
            }
        }
        return endIndex;
    }

    public static int sizeStringToWidthBlind(String s, int maxPx) {
        if (s == null || maxPx <= 0) {
            return 0;
        }
        if (metrics.stringWidth(s) <= maxPx) {
            return s.length();
        }
        int lo = 0;
        int hi = s.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (metrics.stringWidth(s.substring(0, mid)) <= maxPx) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return lo;
    }

    public static int sizeStringToWidth(String s, int maxPx) {
        if (s == null) {
            return 0;
        }
        return metrics.sizeStringToWidth(s, maxPx);
    }

    public static List<String> listFormattedStringToWidth(String str, String wrappedFormatting) {
        if (str == null) {
            return Arrays.asList("");
        }
        return Arrays.asList(wrapStringToWidth(str, BOOK_TEXT_WIDTH, wrappedFormatting).split("\u00b7"));
    }

    public static String wrapStringToWidth(String strIn, int maxWidth, String wrappedFormatting) {
        if (strIn == null) {
            return "";
        }
        int maxCharsInWidth = sizeStringToWidth(wrappedFormatting + strIn, maxWidth) - wrappedFormatting.length();
        if (maxCharsInWidth <= 0) {
            maxCharsInWidth = 1;
        }
        if (strIn.length() <= maxCharsInWidth) {
            return strIn;
        }
        String firstSegment = strIn.substring(0, Math.min(maxCharsInWidth, strIn.length()));
        char splitChar = strIn.charAt(Math.min(maxCharsInWidth, strIn.length() - 1));
        boolean newlineOrSpace = maxCharsInWidth < strIn.length() && (splitChar == ' ' || splitChar == '\n');
        String remainder = strIn.substring(Math.min(maxCharsInWidth + (newlineOrSpace ? 1 : 0), strIn.length()));
        if (newlineOrSpace && maxCharsInWidth < strIn.length()) {
            firstSegment = firstSegment + splitChar;
        }
        String formatting = getActiveFormatting(wrappedFormatting + firstSegment);
        return firstSegment + '\u00b7' + wrapStringToWidth(remainder, maxWidth, formatting);
    }

    public static String getActiveFormatting(String s) {
        return formatter().getActiveFormatting(s);
    }

    public static String getFormatFromString(String str) {
        return formatter().getFormatFromString(str);
    }

    public static boolean isFormatSpecial(char par0) {
        return formatter().isFormatSpecial(par0);
    }

    public static boolean isFormatColor(char par0) {
        return formatter().isFormatColor(par0);
    }

    public static int getStringWidth(String strIn) {
        return metrics.stringWidth(strIn);
    }

    private static BookFormatter formatter() {
        return BookController.getFormatter();
    }
}
