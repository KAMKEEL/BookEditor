package kamkeel.bookeditor.format;

import kamkeel.bookeditor.util.MinecraftTextMetrics;

/**
 * Default formatter used when Hex Text is not installed. It mirrors vanilla
 * Minecraft formatting behaviour.
 */
public class StandaloneFormatter extends BookFormatter {
    public StandaloneFormatter() {
        super(new MinecraftTextMetrics());
    }

    @Override
    public String getName() {
        return "StandaloneFormatter";
    }
}
