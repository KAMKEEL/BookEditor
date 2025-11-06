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
        if (strIn.isEmpty()) {
            return strIn;
        }

        int maxCharsInWidth = Line.sizeStringToWidth(wrappedFormatting + strIn, maxWidth) - wrappedFormatting.length();
        if (maxCharsInWidth <= 0) {
            maxCharsInWidth = 1;
        }
        if (maxCharsInWidth >= strIn.length()) {
            return strIn;
        }

        String prefix = strIn.substring(0, maxCharsInWidth);

        int forcedBreak = prefix.lastIndexOf('\n');
        if (forcedBreak >= 0) {
            int breakPos = forcedBreak + 1;
            String head = strIn.substring(0, breakPos);
            String tail = strIn.substring(breakPos);
            String nextFormatting = collectActiveFormatting(wrappedFormatting + head, options);
            if (tail.isEmpty()) {
                return head;
            }
            return head + Line.SPLIT_CHAR + wrapStringToWidth(tail, maxWidth, nextFormatting, options);
        }

        if (maxCharsInWidth < strIn.length()) {
            char boundary = strIn.charAt(maxCharsInWidth);
            if (boundary != ' ' && boundary != '\n') {
                int lastSpace = prefix.lastIndexOf(' ');
                if (lastSpace >= 0) {
                    maxCharsInWidth = lastSpace + 1;
                    prefix = strIn.substring(0, maxCharsInWidth);
                }
            }
        }

        char boundaryChar = maxCharsInWidth < strIn.length() ? strIn.charAt(maxCharsInWidth) : '\0';
        String remainder = "";
        if (maxCharsInWidth < strIn.length()) {
            int start = maxCharsInWidth;
            if (boundaryChar == ' ' || boundaryChar == '\n') {
                prefix = prefix + boundaryChar;
                start++;
            }
            remainder = strIn.substring(start);
        }

        String nextFormatting = collectActiveFormatting(wrappedFormatting + prefix, options);
        if (remainder.isEmpty()) {
            return prefix;
        }
        return prefix + Line.SPLIT_CHAR + wrapStringToWidth(remainder, maxWidth, nextFormatting, options);
    }
}
