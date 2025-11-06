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
        int maxCharsInWidth = Line.sizeStringToWidth(wrappedFormatting + strIn, maxWidth) - wrappedFormatting.length();
        if (maxCharsInWidth <= 0) {
            maxCharsInWidth = 1;
        }
        if (strIn.length() <= maxCharsInWidth) {
            return strIn;
        }
        int wrapPos = adjustWrapPosition(strIn, maxCharsInWidth, options);
        String s1 = strIn.substring(0, wrapPos);
        char c0 = strIn.charAt(wrapPos);
        boolean newlineOrSpace = c0 == ' ' || c0 == '\n';
        String s2 = strIn.substring(wrapPos + (newlineOrSpace ? 1 : 0));
        if (newlineOrSpace) {
            s1 = s1 + c0;
        }
        wrappedFormatting = collectActiveFormatting(wrappedFormatting + s1, options);
        return s1 + Line.SPLIT_CHAR + wrapStringToWidth(s2, maxWidth, wrappedFormatting, options);
    }

    private static int adjustWrapPosition(String text, int proposedPos, FormattingOptions options) {
        if (proposedPos >= text.length()) {
            return clampToTextLength(text, proposedPos);
        }
        char proposedChar = text.charAt(proposedPos);
        if (proposedChar == ' ' || proposedChar == '\n') {
            return proposedPos;
        }
        int whitespace = findLastWhitespace(text, proposedPos);
        if (whitespace > 0) {
            return whitespace;
        } else if (whitespace == 0) {
            return clampToTextLength(text, proposedPos);
        }
        int formattingStart = FormattingUtil.findFormattingCodeStart(text, proposedPos + 1, options);
        if (formattingStart >= 0) {
            int len = FormattingUtil.detectFormattingCodeLength(text, formattingStart, options);
            if (formattingStart == 0) {
                return clampToTextLength(text, formattingStart + len);
            }
            return formattingStart;
        }
        return proposedPos;
    }

    private static int findLastWhitespace(String text, int beforeIndex) {
        for (int i = beforeIndex - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == ' ' || c == '\n') {
                return i;
            }
        }
        return -1;
    }

    private static int clampToTextLength(String text, int index) {
        int lastIndex = text.length() - 1;
        if (lastIndex < 0) {
            return 0;
        }
        if (index > lastIndex) {
            return lastIndex;
        }
        return Math.max(index, 0);
    }
}
