package kamkeel.bookeditor.util;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * Utility helpers for Angelica specific behaviour. The class exposes
 * safe wrappers when interacting with the player's held book and helper
 * routines for Angelica's extended RGB formatting codes.
 */
public final class AngelicaUtil {
    private AngelicaUtil() {
    }

    /**
     * Returns the item stack currently held by the player or {@code null}
     * if the Minecraft instance/player/held item is not available.
     */
    public static ItemStack safeGetHeldItem(Minecraft minecraft) {
        if (minecraft == null || minecraft.thePlayer == null) {
            return null;
        }
        return minecraft.thePlayer.getHeldItem();
    }

    /**
     * Checks if the provided stack represents a writable book.
     */
    public static boolean isWritableBook(ItemStack stack) {
        return stack != null && stack.getItem() != null && stack.getItem().equals(Items.writable_book);
    }

    /**
     * Checks if the provided stack represents either a writable or written book.
     */
    public static boolean isAnyBook(ItemStack stack) {
        return stack != null && stack.getItem() != null
            && (stack.getItem().equals(Items.writable_book) || stack.getItem().equals(Items.written_book));
    }

    /**
     * Detect the length of an Angelica formatting code starting at {@code index}.
     * Supported codes are:
     * <ul>
     *     <li>Classic Minecraft codes using {@code §}</li>
     *     <li>Ampersand RGB codes like {@code &RRGGBB}</li>
     *     <li>Angelica tag based codes {@code <RRGGBB>} and {@code </RRGGBB>}</li>
     * </ul>
     *
     * @return The number of characters that belong to the code or {@code 0}
     * when no code starts at {@code index}.
     */
    public static int detectAngelicaColorCodeLength(CharSequence text, int index) {
        if (text == null || index < 0 || index >= text.length()) {
            return 0;
        }

        char current = text.charAt(index);
        int remaining = text.length() - index;

        if (current == '§' && remaining >= 2) {
            char code = text.charAt(index + 1);
            if (isLegacyFormatColor(code) || isLegacyFormatSpecial(code)) {
                return 2;
            }
        }

        if (current == '&' && remaining >= 2) {
            char next = text.charAt(index + 1);
            if (isLegacyFormatColor(next) || isLegacyFormatSpecial(next)) {
                return 2;
            }
            if (remaining >= 7 && isValidHex(text, index + 1)) {
                return 7;
            }
        }

        if (current == '<') {
            if (remaining >= 9 && text.charAt(index + 1) == '/' && text.charAt(index + 8) == '>'
                && isValidHex(text, index + 2)) {
                return 9;
            }
            if (remaining >= 8 && text.charAt(index + 7) == '>' && isValidHex(text, index + 1)) {
                return 8;
            }
        }

        return 0;
    }

    /**
     * Attempts to find the start position of a formatting code that ends at {@code endExclusive}.
     * The returned index can be used as the "from" argument when removing the code.
     *
     * @param text         The text that potentially contains a code.
     * @param endExclusive Index immediately after the code.
     * @return The start index of the code or {@code -1} if no code terminates at {@code endExclusive}.
     */
    public static int findAngelicaColorCodeStart(CharSequence text, int endExclusive) {
        if (text == null || endExclusive <= 0 || endExclusive > text.length()) {
            return -1;
        }

        int length = endExclusive;

        if (length >= 2 && text.charAt(length - 2) == '§') {
            char codeChar = text.charAt(length - 1);
            if (isLegacyFormatColor(codeChar) || isLegacyFormatSpecial(codeChar)) {
                return length - 2;
            }
        }

        if (length >= 2 && text.charAt(length - 2) == '&') {
            char codeChar = text.charAt(length - 1);
            if (isLegacyFormatColor(codeChar) || isLegacyFormatSpecial(codeChar)) {
                return length - 2;
            }
        }

        if (length >= 7 && text.charAt(length - 7) == '&' && isValidHex(text, length - 6)) {
            return length - 7;
        }

        if (length >= 8 && text.charAt(length - 8) == '<' && text.charAt(length - 1) == '>'
            && isValidHex(text, length - 7)) {
            return length - 8;
        }

        if (length >= 9 && text.charAt(length - 9) == '<' && text.charAt(length - 8) == '/'
            && text.charAt(length - 1) == '>' && isValidHex(text, length - 7)) {
            return length - 9;
        }

        return -1;
    }

    /**
     * Returns {@code true} when the provided text contains at least one Angelica colour code.
     */
    public static boolean containsAngelicaFormatting(CharSequence text) {
        if (text == null || text.length() == 0) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            int codeLength = detectAngelicaColorCodeLength(text, i);
            if (codeLength > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ensures the text being sent to the server does not contain truncated
     * formatting tokens by discarding incomplete trailing markers.
     */
    public static String sanitizeAngelicaFormatting(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder sanitized = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); ) {
            int codeLength = detectAngelicaColorCodeLength(text, i);
            if (codeLength > 0) {
                if (i + codeLength <= text.length()) {
                    sanitized.append(text, i, i + codeLength);
                    i += codeLength;
                    continue;
                }
                // Incomplete code at the end - drop the remaining characters
                break;
            }
            sanitized.append(text.charAt(i));
            i++;
        }

        return sanitized.toString();
    }

    private static boolean isValidHex(CharSequence text, int start) {
        if (text == null || start < 0 || start + 6 > text.length()) {
            return false;
        }
        for (int i = 0; i < 6; i++) {
            char c = text.charAt(start + i);
            if (!isValidHexChar(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidHexChar(char c) {
        char upper = Character.toUpperCase(c);
        return (upper >= '0' && upper <= '9') || (upper >= 'A' && upper <= 'F');
    }

    private static boolean isLegacyFormatColor(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static boolean isLegacyFormatSpecial(char c) {
        return (c >= 'k' && c <= 'o') || (c >= 'K' && c <= 'O') || c == 'r' || c == 'R';
    }
}
