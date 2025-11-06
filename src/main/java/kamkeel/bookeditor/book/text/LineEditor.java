package kamkeel.bookeditor.book.text;

import java.util.Arrays;
import java.util.List;

import kamkeel.bookeditor.book.Line;
import kamkeel.bookeditor.book.format.FormattingOptions;
import kamkeel.bookeditor.book.format.FormattingUtil;

public final class LineEditor {
    private static final int BOOK_TEXT_WIDTH = 116;

    private LineEditor() {
    }

    public static String insert(Line line, int charPos, String addition, FormattingOptions options) {
        if (charPos < 0) {
            charPos = 0;
        } else if (charPos > line.text.length()) {
            charPos = line.text.length();
        }
        String newText = line.text.substring(0, charPos) + addition + line.text.substring(charPos);
        List<String> lines = splitToWidth(newText, line.wrappedFormatting, options);
        line.text = lines.get(0);
        if (lines.size() == 1) {
            return "";
        }
        StringBuilder overflow = new StringBuilder();
        for (int i = 1; i < lines.size(); i++) {
            overflow.append(lines.get(i));
        }
        return overflow.toString();
    }

    public static List<String> splitToWidth(String text, String wrappedFormatting, FormattingOptions options) {
        return Arrays.asList(wrapStringToWidth(text, BOOK_TEXT_WIDTH, wrappedFormatting, options).split(String.valueOf(Line.SPLIT_CHAR)));
    }

    public static String collectActiveFormatting(String text, FormattingOptions options) {
        return FormattingUtil.collectActiveFormatting(text, options);
    }

    private static String wrapStringToWidth(String strIn, int maxWidth, String wrappedFormatting, FormattingOptions options) {
        int splitIndex = findSplitIndex(strIn, maxWidth, wrappedFormatting, options);
        if (splitIndex >= strIn.length()) {
            return strIn;
        }

        String firstSegment = strIn.substring(0, splitIndex);
        char nextChar = strIn.charAt(splitIndex);
        boolean consumeNext = nextChar == ' ' || nextChar == '\n';
        String remaining = strIn.substring(splitIndex + (consumeNext ? 1 : 0));
        if (consumeNext) {
            firstSegment = firstSegment + nextChar;
        }

        wrappedFormatting = collectActiveFormatting(wrappedFormatting + firstSegment, options);
        return firstSegment + Line.SPLIT_CHAR + wrapStringToWidth(remaining, maxWidth, wrappedFormatting, options);
    }

    private static int findSplitIndex(String text, int maxWidth, String wrappedFormatting, FormattingOptions options) {
        if (text.isEmpty()) {
            return 0;
        }

        int maxChars = Line.sizeStringToWidth(wrappedFormatting + text, maxWidth) - wrappedFormatting.length();
        if (maxChars >= text.length()) {
            return text.length();
        }

        int searchBound = Math.min(maxChars, text.length() - 1);
        int newlineIndex = text.lastIndexOf('\n', searchBound);
        if (newlineIndex >= 0) {
            return newlineIndex;
        }

        int spaceIndex = findLastBreakOpportunity(text, searchBound, options);
        if (spaceIndex >= 0) {
            return spaceIndex;
        }

        return Math.max(1, maxChars);
    }

    private static int findLastBreakOpportunity(String text, int start, FormattingOptions options) {
        for (int i = start; i >= 0; i--) {
            char current = text.charAt(i);
            if (current == ' ' || current == '\n') {
                return i;
            }

            int formattingLength = FormattingUtil.detectFormattingCodeLength(text, i, options);
            if (formattingLength > 0) {
                i -= formattingLength - 1;
            }
        }
        return -1;
    }
}
