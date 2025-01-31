package kamkeel.plugeditor.book;

import java.util.Arrays;
import java.util.List;

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
        float partialCharWidth = (lenPixels - getStringWidth(outStr));
        if ((partialCharWidth / f.getCharWidth(str.charAt(outStr.length()))) > 0.5D)
            return outStr.length() + 1;
        return outStr.length();
    }

    public static int sizeStringToWidthBlind(String str, int maxLenPixels) {
        if (getStringWidth(str) <= maxLenPixels)
            return str.length();
        String outStr = "";
        for (int i = 0; i < str.length(); i++) {
            if (getStringWidth(outStr + str.charAt(i)) > maxLenPixels)
                return outStr.length();
            outStr = outStr + str.charAt(i);
        }
        return outStr.length();
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

    public static int sizeStringToWidth(String par1Str, int par2) {
        int j = par1Str.length();
        int k = 0;
        int l = 0;
        int i1 = -1;
        for (boolean flag = false; l < j; l++) {
            char c0 = par1Str.charAt(l);
            switch (c0) {
                case '\n':
                    l--;
                    break;
                case '\u00a7':
                    if (l < j - 1) {
                        l++;
                        char c1 = par1Str.charAt(l);
                        if (c1 != 'l' && c1 != 'L') {
                            if (c1 == 'r' || c1 == 'R' || isFormatColor(c1))
                                flag = false;
                            break;
                        }
                        flag = true;
                    }
                    break;
                case ' ':
                    i1 = l;
                default:
                    k += (Minecraft.getMinecraft()).fontRenderer.getCharWidth(c0);
                    if (flag)
                        k++;
                    break;
            }
            if (c0 == '\n') {
                i1 = ++l;
                break;
            }
            if (k > par2)
                break;
        }
        return (l != j && i1 != -1 && i1 < l) ? i1 : l;
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

    private static String getActiveFormatting(String strIn) {
        String activeFormatCode = "";
        String activeColorCode = "";
        for (int i = 0; i < strIn.length() - 1; i++) {
            if (strIn.charAt(i) == '\u00a7') {
                char formatChar = strIn.charAt(i + 1);
                if (formatChar == 'r' || formatChar == 'R') {
                    activeFormatCode = "";
                    activeColorCode = "";
                } else if ((formatChar >= '\036' && formatChar <= '\'') || (formatChar >= 'A' && formatChar <= 'F') || (formatChar >= 'a' && formatChar <= 'f')) {
                    activeFormatCode = "";
                    activeColorCode = "\u00a7" + formatChar;
                } else if ((formatChar >= 'K' && formatChar <= 'O') || (formatChar >= 'k' && formatChar <= 'o')) {
                    activeFormatCode = "\u00a7" + formatChar;
                }
            }
        }
        return activeColorCode + activeFormatCode;
    }

    public static int getStringWidth(String strIn) {
        String unfucked = strIn.replaceAll("\u00a7[A-Fa-f0-9]", "\u00a7r$0");
        return (Minecraft.getMinecraft()).fontRenderer.getStringWidth(unfucked);
    }
}