package kamkeel.bookeditor.util;

import kamkeel.bookeditor.util.FontRendererPrivate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * {@link TextMetrics} implementation backed by Minecraft's {@link FontRenderer}.
 * Falls back to {@link SimpleTextMetrics} whenever Minecraft is not available,
 * ensuring server-side tests can still run deterministically.
 */
public class MinecraftTextMetrics implements TextMetrics {
    private final TextMetrics fallback;

    public MinecraftTextMetrics() {
        this(new SimpleTextMetrics());
    }

    public MinecraftTextMetrics(TextMetrics fallback) {
        this.fallback = fallback;
    }

    @Override
    public int stringWidth(String text) {
        FontRenderer renderer = resolveRenderer();
        if (renderer != null) {
            return renderer.getStringWidth(text);
        }
        return fallback.stringWidth(text);
    }

    @Override
    public int charWidth(char character) {
        FontRenderer renderer = resolveRenderer();
        if (renderer != null) {
            return renderer.getCharWidth(character);
        }
        return fallback.charWidth(character);
    }

    @Override
    public int sizeStringToWidth(String text, int maxWidth) {
        FontRenderer renderer = resolveRenderer();
        if (renderer != null) {
            return FontRendererPrivate.callSizeStringToWidth(renderer, text, maxWidth);
        }
        return fallback.sizeStringToWidth(text, maxWidth);
    }

    private FontRenderer resolveRenderer() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft != null ? minecraft.fontRenderer : null;
    }
}
