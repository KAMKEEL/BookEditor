package kamkeel.bookeditor.book;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.HexTextFormatter;
import kamkeel.bookeditor.format.StandaloneFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Central coordination point for book formatting. This controller decides which
 * formatter implementation to use based on the mods available at runtime and
 * exposes the selected formatter to the rest of the codebase.
 */
public final class BookController {
    private static final String HEX_TEXT_MODID = "hextext";
    private static final Logger FALLBACK_LOGGER = LogManager.getLogger("BookEditor");
    private static final BookFormatter STANDALONE_FORMATTER = new StandaloneFormatter();

    private static volatile BookFormatter activeFormatter = STANDALONE_FORMATTER;

    private BookController() {
    }

    public static void preInit(FMLPreInitializationEvent event) {
        Logger logger = event != null ? event.getModLog() : FALLBACK_LOGGER;
        BookFormatter formatter = STANDALONE_FORMATTER;
        if (Loader.isModLoaded(HEX_TEXT_MODID)) {
            try {
                formatter = new HexTextFormatter();
                logger.info("Hex Text detected, enabling enhanced book formatting.");
            } catch (Throwable throwable) {
                logger.warn("Failed to initialise Hex Text formatter, falling back to standalone mode.", throwable);
                formatter = STANDALONE_FORMATTER;
            }
        } else {
            logger.info("Hex Text not detected, using standalone book formatting.");
        }
        setFormatter(formatter);
        logger.info("Active book formatter: {}", activeFormatter.getName());
    }

    public static BookFormatter getFormatter() {
        return activeFormatter;
    }

    public static void setFormatter(BookFormatter formatter) {
        activeFormatter = formatter != null ? formatter : STANDALONE_FORMATTER;
    }

    public static void resetFormatter() {
        activeFormatter = STANDALONE_FORMATTER;
    }
}
