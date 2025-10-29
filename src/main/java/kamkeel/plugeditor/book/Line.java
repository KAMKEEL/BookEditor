package kamkeel.plugeditor.book;

/**
 * Represents a single line of text inside a book page. Handles text wrapping
 * and formatting logic so that the rendered output matches Minecraft's book
 * GUI.
 */

import java.util.List;

import kamkeel.plugeditor.util.BookTextFormatter;
import kamkeel.plugeditor.util.FontRendererPrivate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class Line {
    public static final int BOOK_TEXT_WIDTH = 116;

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
        List<String> newLines = listFormattedStringToWidth(newText, this.wrappedFormatting);
        this.text = newLines.get(0);
        String overflow = "";
        for (int i = 1; i < newLines.size(); i++)
            overflow = overflow + (String) newLines.get(i);
        return overflow;
    }

    public static int sizeStringToApproxWidthBlind(String str, int lenPixels) {
        FontRenderer f = (Minecraft.getMinecraft()).fontRenderer;
        if (getStringWidth(str) <= lenPixels)
            return str.length();
        String outStr = str.substring(0, sizeStringToWidthBlind(str, lenPixels));
        // If the entire string fits, avoid accessing beyond the last index
        if (outStr.length() == str.length()) {
            return outStr.length();
        }

        float partialCharWidth = lenPixels - getStringWidth(outStr);
        if ((partialCharWidth / f.getCharWidth(str.charAt(outStr.length()))) > 0.5D)
            return outStr.length() + 1;
        return outStr.length();
    }

    public static int sizeStringToWidthBlind(String s, int maxPx) {
        FontRenderer f = Minecraft.getMinecraft().fontRenderer;
        if (f.getStringWidth(s) <= maxPx) return s.length();
        int lo=0, hi=s.length();
        while (lo < hi) {
            int mid = (lo+hi+1)/2;
            if (f.getStringWidth(s.substring(0, mid)) <= maxPx) lo = mid; else hi = mid-1;
        }
        return lo;
    }

    public String getTextWithWrappedFormatting() {
        return this.wrappedFormatting + this.text;
    }

    public static int sizeStringToWidth(String s, int maxPx) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        return FontRendererPrivate.callSizeStringToWidth(fr, s, maxPx);
    }

    public static List<String> listFormattedStringToWidth(String str, String wrappedFormatting) {
        FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;
        BookTextFormatter formatter = BookController.getInstance().getTextFormatter();
        return formatter.listFormattedStringToWidth(renderer, str, wrappedFormatting, BOOK_TEXT_WIDTH);
    }

    public String getActiveFormatting() {
        BookTextFormatter formatter = BookController.getInstance().getTextFormatter();
        return formatter.extractActiveFormatting(this.wrappedFormatting + this.text);
    }

    public static int getStringWidth(String strIn) {
        FontRenderer f = Minecraft.getMinecraft().fontRenderer;
        return f.getStringWidth(strIn);
    }
}
