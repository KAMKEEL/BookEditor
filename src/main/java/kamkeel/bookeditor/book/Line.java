package kamkeel.bookeditor.book;

import kamkeel.bookeditor.book.format.FormattingOptions;
import kamkeel.bookeditor.book.text.LineEditor;
import kamkeel.bookeditor.util.FontRendererAccess;
import net.minecraft.client.gui.FontRenderer;

public class Line {
    public static final int BOOK_TEXT_WIDTH = 116;
    public static final char SPLIT_CHAR = '\u00b7';

    public String wrappedFormatting = "";
    public String text = "";

    public String addText(int charPos, String strAdd) {
        return addText(charPos, strAdd, FormattingOptions.defaults());
    }

    public String addText(int charPos, String strAdd, FormattingOptions options) {
        return LineEditor.insert(this, charPos, strAdd, options);
    }

    public static int sizeStringToApproxWidthBlind(String str, int lenPixels) {
        FontRenderer f = FontRendererAccess.get();
        if (getStringWidth(str) <= lenPixels) {
            return str.length();
        }
        String outStr = str.substring(0, sizeStringToWidthBlind(str, lenPixels));
        if (outStr.length() == str.length()) {
            return outStr.length();
        }
        float partialCharWidth = lenPixels - getStringWidth(outStr);
        if ((partialCharWidth / f.getCharWidth(str.charAt(outStr.length()))) > 0.5D) {
            return outStr.length() + 1;
        }
        return outStr.length();
    }

    public static int sizeStringToWidthBlind(String s, int maxPx) {
        FontRenderer f = FontRendererAccess.get();
        if (f.getStringWidth(s) <= maxPx) {
            return s.length();
        }
        int lo = 0;
        int hi = s.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (f.getStringWidth(s.substring(0, mid)) <= maxPx) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return lo;
    }

    public String getTextWithWrappedFormatting() {
        return this.wrappedFormatting + this.text;
    }

    public String getRenderableText() {
        return stripLineTerminators(getTextWithWrappedFormatting());
    }

    private static String stripLineTerminators(String text) {
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == '\n' || c == '\r') {
                StringBuilder builder = new StringBuilder(length - 1);
                if (i > 0) {
                    builder.append(text, 0, i);
                }
                for (int j = i + 1; j < length; j++) {
                    char next = text.charAt(j);
                    if (next != '\n' && next != '\r') {
                        builder.append(next);
                    }
                }
                return builder.toString();
            }
        }
        return text;
    }

    public static int sizeStringToWidth(String s, int maxPx) {
        FontRenderer fr = FontRendererAccess.get();
        return fr.sizeStringToWidth(s, maxPx);
    }

    public static java.util.List<String> listFormattedStringToWidth(String str, String wrappedFormatting, FormattingOptions options) {
        return LineEditor.splitToWidth(str, wrappedFormatting, options);
    }

    public static java.util.List<String> listFormattedStringToWidth(String str, String wrappedFormatting) {
        return listFormattedStringToWidth(str, wrappedFormatting, FormattingOptions.defaults());
    }

    public String getActiveFormatting() {
        return LineEditor.collectActiveFormatting(this.wrappedFormatting + this.text, FormattingOptions.defaults());
    }

    public static int getStringWidth(String strIn) {
        FontRenderer f = FontRendererAccess.get();
        return f.getStringWidth(strIn);
    }
}
