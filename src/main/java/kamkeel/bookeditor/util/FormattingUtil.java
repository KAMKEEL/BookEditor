package kamkeel.bookeditor.util;

/**
 * Utility methods for working with standard Minecraft formatting codes.
 * These helpers replace the Angelica specific formatting logic while still
 * handling vanilla colour/style codes gracefully.
 */
public final class FormattingUtil {
    private FormattingUtil() {
    }

    /**
     * Detects the length of a Minecraft formatting code that starts at the provided index.
     * Returns {@code 0} when no code starts at the index.
     */
    public static int detectFormattingCodeLength(CharSequence text, int index) {
        if (text == null || index < 0 || index >= text.length()) {
            return 0;
        }
        if (text.charAt(index) != '\u00a7') {
            return 0;
        }
        if (index + 1 >= text.length()) {
            return 1; // Incomplete formatting marker - treat as one char.
        }
        char code = text.charAt(index + 1);
        if (isLegacyFormatColor(code) || isLegacyFormatSpecial(code)) {
            return 2;
        }
        return 1;
    }

    /**
     * Attempts to find the starting index of a formatting code that ends at {@code endExclusive}.
     * Returns {@code -1} if no formatting code terminates at that position.
     */
    public static int findFormattingCodeStart(CharSequence text, int endExclusive) {
        if (text == null || endExclusive <= 0 || endExclusive > text.length()) {
            return -1;
        }
        if (endExclusive >= 2 && text.charAt(endExclusive - 2) == '\u00a7') {
            char codeChar = text.charAt(endExclusive - 1);
            if (isLegacyFormatColor(codeChar) || isLegacyFormatSpecial(codeChar)) {
                return endExclusive - 2;
            }
            return endExclusive - 1;
        }
        return -1;
    }

    /**
     * Removes dangling formatting markers that would otherwise crash the client when sent to the server.
     */
    public static String sanitizeFormatting(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sanitized = new StringBuilder(text.length());
        for (int i = 0; i < text.length();) {
            int length = detectFormattingCodeLength(text, i);
            if (length > 0) {
                if (length < 2) {
                    break;
                }
                sanitized.append(text, i, i + 2);
                i += length;
                continue;
            }
            sanitized.append(text.charAt(i));
            i++;
        }
        return sanitized.toString();
    }

    private static boolean isLegacyFormatColor(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static boolean isLegacyFormatSpecial(char c) {
        return (c >= 'k' && c <= 'o') || (c >= 'K' && c <= 'O') || c == 'r' || c == 'R';
    }
}
