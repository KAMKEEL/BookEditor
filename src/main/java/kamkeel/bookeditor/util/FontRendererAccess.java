package kamkeel.bookeditor.util;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Centralises access to Minecraft's {@link FontRenderer}. Production code uses
 * the real renderer obtained from the game instance, while unit tests can swap
 * in a lightweight implementation via {@link #setProvider(Supplier)}.
 */
public final class FontRendererAccess {
    private static Supplier<FontRenderer> provider = new Supplier<FontRenderer>() {
        @Override
        public FontRenderer get() {
            return Minecraft.getMinecraft().fontRenderer;
        }
    };

    private FontRendererAccess() {
    }

    public static FontRenderer get() {
        return provider.get();
    }

    public static void setProvider(Supplier<FontRenderer> customProvider) {
        provider = Objects.requireNonNull(customProvider, "customProvider");
    }

    public static void reset() {
        provider = new Supplier<FontRenderer>() {
            @Override
            public FontRenderer get() {
                return Minecraft.getMinecraft().fontRenderer;
            }
        };
    }
}
