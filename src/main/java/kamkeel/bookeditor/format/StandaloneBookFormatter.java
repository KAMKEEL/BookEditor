package kamkeel.bookeditor.format;

/**
 * Vanilla-compatible formatter that only understands Minecraft's legacy
 * formatting codes. This mirrors the previous built-in behaviour when Hex Text
 * is not present.
 */
public class StandaloneBookFormatter extends BookFormatter {

    @Override
    public int detectFormattingCodeLength(CharSequence text, int index) {
        if (text == null || index < 0 || index >= text.length()) {
            return 0;
        }
        if (text.charAt(index) != '\u00a7') {
            return 0;
        }
        if (index + 1 >= text.length()) {
            return 1;
        }
        char code = text.charAt(index + 1);
        if (isFormatColor(code) || isFormatSpecial(code)) {
            return 2;
        }
        return 1;
    }

    @Override
    public String getActiveFormatting(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String colour = "";
        boolean obfuscated = false;
        boolean bold = false;
        boolean strikethrough = false;
        boolean underline = false;
        boolean italic = false;
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '\u00a7') {
                char c = Character.toLowerCase(text.charAt(i + 1));
                if (c == 'r') {
                    colour = "";
                    obfuscated = bold = strikethrough = underline = italic = false;
                } else if (isFormatColor(c)) {
                    colour = "\u00a7" + c;
                    obfuscated = bold = strikethrough = underline = italic = false;
                } else if (c == 'k') {
                    obfuscated = true;
                } else if (c == 'l') {
                    bold = true;
                } else if (c == 'm') {
                    strikethrough = true;
                } else if (c == 'n') {
                    underline = true;
                } else if (c == 'o') {
                    italic = true;
                }
            }
        }
        StringBuilder out = new StringBuilder();
        if (!colour.isEmpty()) out.append(colour);
        if (obfuscated) out.append("\u00a7k");
        if (bold) out.append("\u00a7l");
        if (strikethrough) out.append("\u00a7m");
        if (underline) out.append("\u00a7n");
        if (italic) out.append("\u00a7o");
        return out.toString();
    }

    @Override
    public String getFormatFromString(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String result = "";
        int index = -1;
        int length = text.length();
        while ((index = text.indexOf('\u00a7', index + 1)) != -1) {
            if (index < length - 1) {
                char c = text.charAt(index + 1);
                if (isFormatColor(c)) {
                    result = "\u00a7" + c;
                    continue;
                }
                if (isFormatSpecial(c)) {
                    result = result + "\u00a7" + c;
                }
            }
        }
        return result;
    }

    @Override
    public boolean isFormatColor(char character) {
        return (character >= '0' && character <= '9')
            || (character >= 'a' && character <= 'f')
            || (character >= 'A' && character <= 'F');
    }

    @Override
    public boolean isFormatSpecial(char character) {
        return (character >= 'k' && character <= 'o')
            || (character >= 'K' && character <= 'O')
            || character == 'r'
            || character == 'R';
    }

    @Override
    protected boolean isCompleteFormattingCode(CharSequence text, int index, int detectedLength) {
        return super.isCompleteFormattingCode(text, index, detectedLength) && detectedLength >= 2;
    }
}
