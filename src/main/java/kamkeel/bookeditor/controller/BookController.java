package kamkeel.bookeditor.controller;

import cpw.mods.fml.common.Loader;
import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.HexTextBookFormatter;
import kamkeel.bookeditor.format.StandaloneBookFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Central place that decides which formatter implementation should be used at
 * runtime. The controller checks whether Hex Text is present and falls back to
 * the vanilla formatter otherwise.
 */
public final class BookController {
    private static final Logger LOGGER = LogManager.getLogger("BookEditor");
    private static final String HEX_TEXT_MODID = "hextext";

    private static final BookFormatter STANDALONE = StandaloneBookFormatter.INSTANCE;
    private static volatile BookFormatter activeFormatter = STANDALONE;

    private BookController() {
    }

    public static void preInit() {
        activeFormatter = chooseFormatter();
    }

    private static BookFormatter chooseFormatter() {
        if (Loader.isModLoaded(HEX_TEXT_MODID)) {
            try {
                HexTextBookFormatter.ensureProxy();
                return new HexTextBookFormatter();
            } catch (Throwable t) {
                LOGGER.warn("Failed to initialise Hex Text formatter, falling back to standalone implementation", t);
            }
        }
        return STANDALONE;
    }

    public static BookFormatter getFormatter() {
        return activeFormatter;
    }

    public static boolean isHexTextFormatterActive() {
        return activeFormatter instanceof HexTextBookFormatter;
    }

    public static void resetFormatter() {
        activeFormatter = STANDALONE;
    }

    public static void setFormatterForTesting(BookFormatter formatter) {
        activeFormatter = formatter != null ? formatter : STANDALONE;
    }
}
