package kamkeel.plugeditor.util;

import net.minecraft.client.gui.FontRenderer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class FontRendererPrivate {
    private static volatile Method SIZE_TO_WIDTH; // int sizeStringToWidth(String, int)

    private FontRendererPrivate() {}

    // Resolve and cache the private method (MCP first, SRG fallback)
    private static Method resolve() {
        Method m = SIZE_TO_WIDTH;
        if (m != null) return m;

        synchronized (FontRendererPrivate.class) {
            if (SIZE_TO_WIDTH != null) return SIZE_TO_WIDTH;
            final Class<FontRenderer> cls = FontRenderer.class;
            final Class<?>[] sig = new Class<?>[]{String.class, int.class};
            NoSuchMethodException first = null;

            try {
                m = cls.getDeclaredMethod("sizeStringToWidth", sig); // MCP dev name
            } catch (NoSuchMethodException e) {
                first = e;
                try {
                    m = cls.getDeclaredMethod("func_78259_e", sig); // SRG 1.7.10
                } catch (NoSuchMethodException e2) {
                    // Both failed—bubble an informative exception
                    NoSuchMethodError err = new NoSuchMethodError(
                        "FontRenderer.sizeStringToWidth/func_78259_e(String,int) not found. " +
                        "Dev mappings or environment might be mismatched."
                    );
                    err.initCause(first); // keep the first failure for context
                    throw err;
                }
            }

            m.setAccessible(true);
            SIZE_TO_WIDTH = m;
            return m;
        }
    }

    /**
     * Calls the exact (possibly mixin-patched) FontRenderer.sizeStringToWidth.
     * @param fr the FontRenderer (must be non-null and initialized)
     * @param text text to measure
     * @param width max width in pixels
     * @return number of characters that fit according to the real method
     */
    public static int callSizeStringToWidth(FontRenderer fr, String text, int width) {
        if (fr == null) {
            throw new IllegalStateException("FontRenderer is null. Call after Minecraft client is initialized.");
        }
        try {
            return (int) resolve().invoke(fr, text, width);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("FontRenderer.sizeStringToWidth not accessible (setAccessible failed?)", e);
        } catch (InvocationTargetException e) {
            // Unwrap the target’s exception for easier debugging
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new RuntimeException("Invocation of sizeStringToWidth failed: " + cause, cause);
        }
    }
}
