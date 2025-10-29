package kamkeel.plugeditor.util;

import java.util.ArrayList;
import java.util.List;

import kamkeel.hextext.client.render.FontRendererUtils;
import kamkeel.hextext.common.util.ColorCodeUtils;
import kamkeel.hextext.common.util.StringUtils;
import net.minecraft.client.gui.FontRenderer;

/**
 * HexText-backed implementation of {@link BookTextFormatter}. This class should
 * only be referenced when the HexText mod is present on the classpath.
 */
public final class HexTextCompat implements BookTextFormatter {
    public static final HexTextCompat INSTANCE = new HexTextCompat();

    private HexTextCompat() {
    }

    @Override
    public List<String> listFormattedStringToWidth(FontRenderer renderer, String text, String wrappedFormatting, int maxWidth) {
        if (renderer == null) {
            return BookUtils.INSTANCE.listFormattedStringToWidth(renderer, text, wrappedFormatting, maxWidth);
        }

        try {
            String prefix = wrappedFormatting == null ? "" : wrappedFormatting;
            String combined = prefix + (text == null ? "" : text);
            String wrapped = FontRendererUtils.wrapFormattedString(renderer, combined, maxWidth, true);
            if (wrapped == null) {
                return BookUtils.INSTANCE.listFormattedStringToWidth(renderer, text, wrappedFormatting, maxWidth);
            }

            String[] split = wrapped.split("\n", -1);
            List<String> lines = new ArrayList<String>(split.length);
            for (int i = 0; i < split.length; i++) {
                String line = split[i];
                if (i == 0 && !prefix.isEmpty() && line.startsWith(prefix)) {
                    line = line.substring(prefix.length());
                }
                lines.add(line);
            }
            if (lines.isEmpty()) {
                return BookUtils.INSTANCE.listFormattedStringToWidth(renderer, text, wrappedFormatting, maxWidth);
            }
            return lines;
        } catch (Throwable throwable) {
            System.out.println("[BookEditor] HexText wrapFormattedString invocation failed: " + throwable.getMessage());
            return BookUtils.INSTANCE.listFormattedStringToWidth(renderer, text, wrappedFormatting, maxWidth);
        }
    }

    @Override
    public int detectColorCodeLength(CharSequence text, int index) {
        if (text == null) {
            return 0;
        }
        try {
            return ColorCodeUtils.detectColorCodeLengthIgnoringRaw(text, index);
        } catch (Throwable throwable) {
            System.out.println("[BookEditor] HexText detectColorCodeLength invocation failed: " + throwable.getMessage());
            return BookUtils.INSTANCE.detectColorCodeLength(text, index);
        }
    }

    @Override
    public int findColorCodeStart(CharSequence text, int endExclusive) {
        if (text == null || endExclusive <= 0 || endExclusive > text.length()) {
            return -1;
        }
        for (int i = endExclusive - 1; i >= 0; i--) {
            int length = detectColorCodeLength(text, i);
            if (length > 0 && i + length == endExclusive) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String sanitizeFormatting(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sanitized = new StringBuilder(text.length());
        for (int i = 0; i < text.length();) {
            int codeLength = detectColorCodeLength(text, i);
            if (codeLength > 0) {
                if (i + codeLength > text.length()) {
                    break;
                }
                sanitized.append(text, i, i + codeLength);
                i += codeLength;
            } else {
                sanitized.append(text.charAt(i));
                i++;
            }
        }
        return sanitized.toString();
    }

    @Override
    public String extractActiveFormatting(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        try {
            return StringUtils.extractFormatFromString(text);
        } catch (Throwable throwable) {
            System.out.println("[BookEditor] HexText extractFormatFromString invocation failed: " + throwable.getMessage());
            return BookUtils.INSTANCE.extractActiveFormatting(text);
        }
    }

    @Override
    public String stripColorCodes(CharSequence text) {
        if (text == null) {
            return null;
        }
        try {
            return StringUtils.stripColorCodes(text);
        } catch (Throwable throwable) {
            System.out.println("[BookEditor] HexText stripColorCodes invocation failed: " + throwable.getMessage());
            return BookUtils.INSTANCE.stripColorCodes(text);
        }
    }
}
