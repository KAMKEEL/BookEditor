package kamkeel.bookeditor.controller;

import cpw.mods.fml.common.Loader;
import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.HexTextFormatter;
import kamkeel.bookeditor.format.StandaloneBookFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Coordinates integration points that depend on optional mods. The controller
 * decides which {@link BookFormatter} implementation should be used at runtime
 * and exposes it to the rest of the mod.
 */
public final class BookController {
    public static final String HEXTEXT_MOD_ID = "hextext";

    private static final Logger LOGGER = LogManager.getLogger("BookEditor");

    private static final BookFormatter STANDALONE_FORMATTER = new StandaloneBookFormatter();

    private static volatile BookFormatter activeFormatter = STANDALONE_FORMATTER;

    private BookController() {
    }

    public static void preInit() {
        activeFormatter = determineFormatter();
    }

    private static BookFormatter determineFormatter() {
        try {
            if (Loader.isModLoaded(HEXTEXT_MOD_ID)) {
                try {
                    return new HexTextFormatter();
                } catch (Throwable t) {
                    LOGGER.warn("Failed to initialise Hex Text integration, falling back to standalone formatter.", t);
                }
            }
        } catch (NoClassDefFoundError error) {
            LOGGER.debug("Forge Loader not available, defaulting to standalone formatter.");
        }
        return STANDALONE_FORMATTER;
    }

    public static BookFormatter getFormatter() {
        return activeFormatter;
    }

    public static void setFormatterForTesting(BookFormatter formatter) {
        activeFormatter = formatter != null ? formatter : STANDALONE_FORMATTER;
    }
}
