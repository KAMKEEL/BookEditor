package kamkeel.bookeditor.book;

/**
 * Represents a single line of text inside a book page. Handles text wrapping
 * and formatting logic so that the rendered output matches Minecraft's book
 * GUI.
 */

import kamkeel.bookeditor.util.FontRendererPrivate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.Arrays;
import java.util.List;

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
        int lo = 0, hi = s.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (f.getStringWidth(s.substring(0, mid)) <= maxPx) lo = mid;
            else hi = mid - 1;
        }
        return lo;
    }

    public String getTextWithWrappedFormatting() {
        return this.wrappedFormatting + this.text;
    }

    private static String getFormatFromString(String par0Str) {
        String s1 = "";
        int i = -1;
        int j = par0Str.length();
        while ((i = par0Str.indexOf('\u00a7', i + 1)) != -1) {
            if (i < j - 1) {
                char c0 = par0Str.charAt(i + 1);
                if (isFormatColor(c0)) {
                    s1 = "\u00a7" + c0;
                    continue;
                }
                if (isFormatSpecial(c0))
                    s1 = s1 + "\u00a7" + c0;
            }
        }
        return s1;
    }

    protected static boolean isFormatSpecial(char par0) {
        return ((par0 >= 'k' && par0 <= 'o') || (par0 >= 'K' && par0 <= 'O') || par0 == 'r' || par0 == 'R');
    }

    protected static boolean isFormatColor(char par0) {
        return ((par0 >= '0' && par0 <= '9') || (par0 >= 'a' && par0 <= 'f') || (par0 >= 'A' && par0 <= 'F'));
    }

    public static int sizeStringToWidth(String s, int maxPx) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        return FontRendererPrivate.callSizeStringToWidth(fr, s, maxPx);
    }

    private static String wrapStringToWidth(String strIn, int maxWidth, String wrappedFormatting) {
        int maxCharsInWidth = sizeStringToWidth(wrappedFormatting + strIn, maxWidth) - wrappedFormatting.length();
        if (strIn.length() <= maxCharsInWidth)
            return strIn;
        String s1 = strIn.substring(0, maxCharsInWidth);
        char c0 = strIn.charAt(maxCharsInWidth);
        boolean newlineOrSpace = (c0 == ' ' || c0 == '\n');
        String s2 = strIn.substring(maxCharsInWidth + (newlineOrSpace ? 1 : 0));
        if (newlineOrSpace)
            s1 = s1 + c0;
        wrappedFormatting = getActiveFormatting(wrappedFormatting + s1);
        return s1 + '\u00b7' + wrapStringToWidth(s2, maxWidth, wrappedFormatting);
    }

    public static List<String> listFormattedStringToWidth(String str, String wrappedFormatting) {
        return Arrays.asList(wrapStringToWidth(str, 116, wrappedFormatting).split("\u00b7"));
    }

    public String getActiveFormatting() {
        return getActiveFormatting(this.wrappedFormatting + this.text);
    }

    private static String getActiveFormatting(String s) {
        String color = "";
        boolean k = false, l = false, m = false, n = false, o = false;

        for (int i = 0; i < s.length() - 1; i++)
            if (s.charAt(i) == '§') {
                char c = Character.toLowerCase(s.charAt(i + 1));
                if (c == 'r') {
                    color = "";
                    k = l = m = n = o = false;
                } else if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')) {
                    color = "§" + c;
                    k = l = m = n = o = false;
                } else if (c == 'k') k = true;
                else if (c == 'l') l = true;
                else if (c == 'm') m = true;
                else if (c == 'n') n = true;
                else if (c == 'o') o = true;
            }
        StringBuilder out = new StringBuilder();
        if (!color.isEmpty()) out.append(color);
        if (k) out.append("§k");
        if (l) out.append("§l");
        if (m) out.append("§m");
        if (n) out.append("§n");
        if (o) out.append("§o");
        return out.toString();
    }

    public static int getStringWidth(String strIn) {
        FontRenderer f = Minecraft.getMinecraft().fontRenderer;
        return f.getStringWidth(strIn);
    }
}
