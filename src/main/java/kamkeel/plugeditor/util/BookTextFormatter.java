package kamkeel.plugeditor.util;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;

/**
 * Abstraction over the formatting logic used by the book editor. Implementations
 * can either use the vanilla-style routines (BookUtils) or delegate to HexText
 * when it is available.
 */
public interface BookTextFormatter {
    List<String> listFormattedStringToWidth(FontRenderer renderer, String text, String wrappedFormatting, int maxWidth);

    int detectColorCodeLength(CharSequence text, int index);

    int findColorCodeStart(CharSequence text, int endExclusive);

    String sanitizeFormatting(String text);

    String extractActiveFormatting(String text);

    String stripColorCodes(CharSequence text);
}
