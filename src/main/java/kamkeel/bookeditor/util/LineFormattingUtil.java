package kamkeel.bookeditor.util;

import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.StandaloneBookFormatter;

import java.util.Arrays;
import java.util.List;

/**
 * Collection of helpers related to measuring and wrapping line content.
 * The heavy lifting used to live inside {@code Line}; extracting it keeps the
 * data object lightweight and makes testing easier.
 */
public final class LineFormattingUtil {
    public static final int BOOK_TEXT_WIDTH = 116;

    private static volatile BookFormatter formatter = new StandaloneBookFormatter();
    private static volatile TextMetrics metrics = new MinecraftTextMetrics();

    private LineFormattingUtil() {
    }

    public static void setFormatter(BookFormatter newFormatter) {
        formatter = newFormatter != null ? newFormatter : new StandaloneBookFormatter();
    }

    private static BookFormatter getFormatter() {
        return formatter;
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
        int newlineIndex = strIn.indexOf('\n');
        if (newlineIndex >= 0 && newlineIndex < strIn.length() - 1) {
            String firstSegment = strIn.substring(0, newlineIndex + 1);
            String remainder = strIn.substring(newlineIndex + 1);
            String formatting = getActiveFormatting(wrappedFormatting + firstSegment);
            return firstSegment + '\u00b7' + wrapStringToWidth(remainder, maxWidth, formatting);
        }
        int maxCharsInWidth = sizeStringToWidth(wrappedFormatting + strIn, maxWidth) - wrappedFormatting.length();
        if (maxCharsInWidth <= 0) {
            maxCharsInWidth = 1;
        }
        if (strIn.length() <= maxCharsInWidth) {
            return strIn;
        }
        int breakIndex = Math.min(maxCharsInWidth, strIn.length());
        if (breakIndex < strIn.length()) {
            int wordBoundary = findLastBreakOpportunity(strIn, breakIndex);
            if (wordBoundary >= 0) {
                breakIndex = wordBoundary + 1;
            }
        }

        String firstSegment = strIn.substring(0, Math.min(breakIndex, strIn.length()));
        int remainderStart = Math.min(breakIndex, strIn.length());
        if (remainderStart < strIn.length()) {
            remainderStart = consumeBreakWhitespace(strIn, remainderStart);
            firstSegment = strIn.substring(0, Math.min(remainderStart, strIn.length()));
        }
        String remainder = strIn.substring(Math.min(remainderStart, strIn.length()));
        String formatting = getActiveFormatting(wrappedFormatting + firstSegment);
        return firstSegment + '\u00b7' + wrapStringToWidth(remainder, maxWidth, formatting);
    }

    private static int findLastBreakOpportunity(String text, int limitExclusive) {
        if (text == null || limitExclusive <= 0) {
            return -1;
        }
        int index = Math.min(limitExclusive, text.length()) - 1;
        while (index >= 0) {
            int formattingStart = FormattingUtil.findFormattingCodeStart(text, index + 1);
            if (formattingStart >= 0) {
                int length = FormattingUtil.detectFormattingCodeLength(text, formattingStart);
                if (length > 0 && formattingStart <= index && index < formattingStart + length) {
                    index = formattingStart - 1;
                    continue;
                }
            }
            char current = text.charAt(index);
            if (current == ' ' || current == '\n') {
                return index;
            }
            index--;
        }
        return -1;
    }

    private static int consumeBreakWhitespace(String text, int startIndex) {
        int index = Math.max(0, startIndex);
        int length = text.length();
        while (index < length) {
            char c = text.charAt(index);
            if (c == ' ' || c == '\n') {
                index++;
                continue;
            }
            break;
        }
        return index;
    }

    public static String getActiveFormatting(String s) {
        return getFormatter().getActiveFormatting(s);
    }

    public static String getFormatFromString(String str) {
        return getFormatter().getFormatFromString(str);
    }

    public static boolean isFormatSpecial(char par0) {
        return getFormatter().isFormatSpecial(par0);
    }

    public static boolean isFormatColor(char par0) {
        return getFormatter().isFormatColor(par0);
    }

    public static int getStringWidth(String strIn) {
        return metrics.stringWidth(strIn);
    }
}
