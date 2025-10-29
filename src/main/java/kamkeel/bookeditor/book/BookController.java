package kamkeel.bookeditor.book;

import cpw.mods.fml.common.Loader;
import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.HexTextBookFormatter;
import kamkeel.bookeditor.format.StandaloneBookFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Coordinates integration points that depend on optional mods such as Hex Text.
 * The controller resolves the appropriate {@link BookFormatter} during
 * {@code preInit} and exposes it to the rest of the mod.
 */
public final class BookController {
    private static final Logger LOGGER = LogManager.getLogger("BookEditor|Controller");
    private static final String HEXTEXT_MODID = "hextext";

    private static final BookFormatter STANDALONE_FORMATTER = new StandaloneBookFormatter();
    private static volatile BookFormatter activeFormatter = STANDALONE_FORMATTER;

    private BookController() {
    }

    public static void preInit() {
        if (Loader.isModLoaded(HEXTEXT_MODID)) {
            try {
                activeFormatter = new HexTextBookFormatter();
                LOGGER.info("Hex Text detected - enabling RGB formatter support.");
            } catch (Throwable t) {
                activeFormatter = STANDALONE_FORMATTER;
                LOGGER.warn("Failed to initialise Hex Text formatter, falling back to standalone implementation.", t);
            }
        } else {
            activeFormatter = STANDALONE_FORMATTER;
            LOGGER.info("Hex Text not detected - using standalone formatter.");
        }
    }

    public static BookFormatter getFormatter() {
        return activeFormatter;
    }

    public static BookFormatter getStandaloneFormatter() {
        return STANDALONE_FORMATTER;
    }

    public static void overrideFormatterForTesting(BookFormatter formatter) {
        activeFormatter = formatter != null ? formatter : STANDALONE_FORMATTER;
    }
}
