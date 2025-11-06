package kamkeel.bookeditor.book.format;

import static java.lang.Character.toLowerCase;

/**
 * Shared formatting helpers used throughout the editor. The logic recognises
 * both vanilla Minecraft formatting codes and additional syntaxes exposed by
 * the optional Hex Text mod.
 */
public final class FormattingUtil {
    private static final char SECTION_SIGN = '\u00a7';
    private static final char AMPERSAND = '&';
    private static final char HEX_PREFIX = '#';

    private FormattingUtil() {
    }

    public static int detectFormattingCodeLength(CharSequence text, int index, FormattingOptions options) {
        if (text == null || index < 0 || index >= text.length()) {
            return 0;
        }
        char current = text.charAt(index);
        if (isSectionPrefix(current) || isAmpersandPrefix(current, options)) {
            if (index + 1 >= text.length()) {
                return 0;
            }
            char codeChar = text.charAt(index + 1);
            if (codeChar == HEX_PREFIX) {
                if (options.hexSectionSupport() && hasHex(text, index + 2, 6)) {
                    return 8;
                }
                return 0;
            }
            if (isFormatCode(codeChar)) {
                return 2;
            }
            return 0;
        }
        if (current == '<' && options.htmlSupport()) {
            if (index + 7 < text.length() && text.charAt(index + 7) == '>' && hasHex(text, index + 1, 6)) {
                return 8; // <RRGGBB>
            }
            if (index + 8 < text.length() && text.charAt(index + 1) == HEX_PREFIX
                && text.charAt(index + 8) == '>' && hasHex(text, index + 2, 6)) {
                return 9; // <#RRGGBB>
            }
            if (index + 8 < text.length() && text.charAt(index + 1) == '/' && text.charAt(index + 8) == '>'
                && hasHex(text, index + 2, 6)) {
                return 9; // </RRGGBB>
            }
            if (index + 9 < text.length() && text.charAt(index + 1) == '/' && text.charAt(index + 2) == HEX_PREFIX
                && text.charAt(index + 9) == '>' && hasHex(text, index + 3, 6)) {
                return 10; // </#RRGGBB>
            }
        }
        return 0;
    }

    public static int findFormattingCodeStart(CharSequence text, int endExclusive, FormattingOptions options) {
        if (text == null || endExclusive <= 0 || endExclusive > text.length()) {
            return -1;
        }
        int maxLookbehind = Math.min(10, endExclusive);
        for (int start = endExclusive - maxLookbehind; start < endExclusive; start++) {
            int len = detectFormattingCodeLength(text, start, options);
            if (len > 0 && start + len == endExclusive) {
                return start;
            }
        }
        return -1;
    }

    public static boolean containsFormatting(CharSequence text, FormattingOptions options) {
        if (text == null || text.length() == 0) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            int len = detectFormattingCodeLength(text, i, options);
            if (len > 0) {
                return true;
            }
        }
        return false;
    }

    public static String sanitizeFormatting(String text, FormattingOptions options) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sanitized = new StringBuilder(text.length());
        int index = 0;
        while (index < text.length()) {
            int len = detectFormattingCodeLength(text, index, options);
            if (len > 0) {
                if (index + len <= text.length()) {
                    sanitized.append(text, index, index + len);
                    index += len;
                    continue;
                }
                break;
            }
            char current = text.charAt(index);
            if (current == SECTION_SIGN) {
                if (index + 1 >= text.length() || text.charAt(index + 1) == HEX_PREFIX) {
                    break;
                }
            } else if (options.ampersandSupport() && current == AMPERSAND) {
                if (index + 1 >= text.length() || text.charAt(index + 1) == HEX_PREFIX) {
                    break;
                }
            } else if (options.htmlSupport() && current == '<') {
                break;
            }
            sanitized.append(current);
            index++;
        }
        return sanitized.toString();
    }

    public static String collectActiveFormatting(String text, FormattingOptions options) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String color = "";
        boolean obfuscated = false;
        boolean bold = false;
        boolean strikethrough = false;
        boolean underline = false;
        boolean italic = false;
        boolean effectG = false;
        boolean effectH = false;
        boolean effectI = false;
        boolean effectJ = false;

        int i = 0;
        while (i < text.length()) {
            int len = detectFormattingCodeLength(text, i, options);
            if (len > 0) {
                char prefix = text.charAt(i);
                if (prefix == SECTION_SIGN || (prefix == AMPERSAND && options.ampersandSupport())) {
                    char code = toLowerCase(text.charAt(i + 1));
                    if (code == HEX_PREFIX && len >= 8) {
                        color = SECTION_SIGN + "#" + text.subSequence(i + 2, i + 8).toString();
                        obfuscated = bold = strikethrough = underline = italic = false;
                        effectG = effectH = effectI = effectJ = false;
                    } else if (isColorCode(code)) {
                        color = SECTION_SIGN + Character.toString(code);
                        obfuscated = bold = strikethrough = underline = italic = false;
                        effectG = effectH = effectI = effectJ = false;
                    } else if (isObfuscated(code)) {
                        obfuscated = true;
                    } else if (isBold(code)) {
                        bold = true;
                    } else if (isStrikethrough(code)) {
                        strikethrough = true;
                    } else if (isUnderline(code)) {
                        underline = true;
                    } else if (isItalic(code)) {
                        italic = true;
                    } else if (isEffectG(code)) {
                        effectG = true;
                    } else if (isEffectH(code)) {
                        effectH = true;
                    } else if (isEffectI(code)) {
                        effectI = true;
                    } else if (isEffectJ(code)) {
                        effectJ = true;
                    } else if (isResetCode(code)) {
                        color = "";
                        obfuscated = bold = strikethrough = underline = italic = false;
                        effectG = effectH = effectI = effectJ = false;
                    }
                } else if (prefix == '<' && options.htmlSupport()) {
                    if (len == 8) { // <RRGGBB>
                        color = SECTION_SIGN + "#" + text.subSequence(i + 1, i + 7).toString();
                        obfuscated = bold = strikethrough = underline = italic = false;
                        effectG = effectH = effectI = effectJ = false;
                    } else if (len == 9 && text.charAt(i + 1) == HEX_PREFIX) { // <#RRGGBB>
                        color = SECTION_SIGN + "#" + text.subSequence(i + 2, i + 8).toString();
                        obfuscated = bold = strikethrough = underline = italic = false;
                        effectG = effectH = effectI = effectJ = false;
                    } else if (len >= 9 && text.charAt(i + 1) == '/') {
                        color = "";
                        obfuscated = bold = strikethrough = underline = italic = false;
                        effectG = effectH = effectI = effectJ = false;
                    }
                }
                i += Math.max(len, 1);
                continue;
            }
            i++;
        }

        StringBuilder out = new StringBuilder();
        if (!color.isEmpty()) {
            out.append(color);
        }
        if (obfuscated) {
            out.append("\u00a7k");
        }
        if (bold) {
            out.append("\u00a7l");
        }
        if (strikethrough) {
            out.append("\u00a7m");
        }
        if (underline) {
            out.append("\u00a7n");
        }
        if (italic) {
            out.append("\u00a7o");
        }
        if (effectG) {
            out.append("\u00a7g");
        }
        if (effectH) {
            out.append("\u00a7h");
        }
        if (effectI) {
            out.append("\u00a7i");
        }
        if (effectJ) {
            out.append("\u00a7j");
        }
        return out.toString();
    }

    private static boolean hasHex(CharSequence text, int start, int len) {
        if (start < 0 || len < 0 || start + len > text.length()) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (!isHexChar(text.charAt(start + i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHexChar(char c) {
        char lower = toLowerCase(c);
        return (lower >= '0' && lower <= '9') || (lower >= 'a' && lower <= 'f');
    }

    private static boolean isFormatCode(char c) {
        char lower = toLowerCase(c);
        return isColorCode(lower) || isStyleCode(lower) || isResetCode(lower)
            || isEffectG(lower) || isEffectH(lower) || isEffectI(lower) || isEffectJ(lower);
    }

    private static boolean isColorCode(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
    }

    private static boolean isStyleCode(char c) {
        return c == 'k' || c == 'l' || c == 'm' || c == 'n' || c == 'o';
    }
    private static boolean isObfuscated(char c) {
        return c == 'k';
    }

    private static boolean isBold(char c) {
        return c == 'l';
    }

    private static boolean isStrikethrough(char c) {
        return c == 'm';
    }

    private static boolean isUnderline(char c) {
        return c == 'n';
    }

    private static boolean isItalic(char c) {
        return c == 'o';
    }

    private static boolean isEffectG(char c) {
        return c == 'g';
    }

    private static boolean isEffectH(char c) {
        return c == 'h';
    }

    private static boolean isEffectI(char c) {
        return c == 'i';
    }

    private static boolean isEffectJ(char c) {
        return c == 'j';
    }

    private static boolean isResetCode(char c) {
        return c == 'r';
    }

    private static boolean isSectionPrefix(char c) {
        return c == SECTION_SIGN;
    }

    private static boolean isAmpersandPrefix(char c, FormattingOptions options) {
        return c == AMPERSAND && options.ampersandSupport();
    }
}
