package kamkeel.bookeditor.book;

import kamkeel.bookeditor.util.LineFormattingUtil;

import java.util.List;

/**
 * Represents a single line of text inside a book page. Handles text wrapping
 * and formatting logic so that the rendered output matches Minecraft's book
 * GUI.
 */
public class Line {
    public static final int BOOK_TEXT_WIDTH = LineFormattingUtil.BOOK_TEXT_WIDTH;

    public static final char SPLIT_CHAR = '\u00b7';

    public String wrappedFormatting = "";

    public String text = "";

    public String addText(int charPos, String strAdd) {
        if (charPos < 0) {
            charPos = 0;
        } else if (charPos > this.text.length()) {
            charPos = this.text.length();
        }
        String newText = this.text.substring(0, charPos) + strAdd + this.text.substring(charPos);
        List<String> newLines = LineFormattingUtil.listFormattedStringToWidth(newText, this.wrappedFormatting);
        this.text = newLines.get(0);
        StringBuilder overflow = new StringBuilder();
        for (int i = 1; i < newLines.size(); i++) {
            overflow.append(newLines.get(i));
        }
        return overflow.toString();
    }

    public static int sizeStringToApproxWidthBlind(String str, int lenPixels) {
        return LineFormattingUtil.sizeStringToApproxWidthBlind(str, lenPixels);
    }

    public static int sizeStringToWidthBlind(String s, int maxPx) {
        return LineFormattingUtil.sizeStringToWidthBlind(s, maxPx);
    }

    public static int sizeStringToWidth(String s, int maxPx) {
        return LineFormattingUtil.sizeStringToWidth(s, maxPx);
    }

    public static List<String> listFormattedStringToWidth(String str, String wrappedFormatting) {
        return LineFormattingUtil.listFormattedStringToWidth(str, wrappedFormatting);
    }

    public String getTextWithWrappedFormatting() {
        return this.wrappedFormatting + this.text;
    }

    public String getActiveFormatting() {
        return LineFormattingUtil.getActiveFormatting(getTextWithWrappedFormatting());
    }

    public static String getFormatFromString(String par0Str) {
        return LineFormattingUtil.getFormatFromString(par0Str);
    }

    protected static boolean isFormatSpecial(char par0) {
        return LineFormattingUtil.isFormatSpecial(par0);
    }

    protected static boolean isFormatColor(char par0) {
        return LineFormattingUtil.isFormatColor(par0);
    }

    public static String getActiveFormatting(String s) {
        return LineFormattingUtil.getActiveFormatting(s);
    }

    public static int getStringWidth(String strIn) {
        return LineFormattingUtil.getStringWidth(strIn);
    }
}
