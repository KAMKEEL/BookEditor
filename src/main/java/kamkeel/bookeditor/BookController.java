package kamkeel.bookeditor;

import cpw.mods.fml.common.Loader;
import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.HexTextFormatter;
import kamkeel.bookeditor.format.StandaloneBookFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Central coordinator responsible for selecting which {@link BookFormatter}
 * implementation should be used. The controller checks for optional
 * dependencies during mod initialisation and exposes the active formatter to
 * the rest of the code base.
 */
public final class BookController {
    private static final Logger LOGGER = LogManager.getLogger("BookEditor");
    private static final BookFormatter STANDALONE_FORMATTER = new StandaloneBookFormatter();

    private static volatile BookFormatter activeFormatter = STANDALONE_FORMATTER;
    private static volatile boolean testingOverride;

    private BookController() {
    }

    public static void preInit() {
        if (testingOverride) {
            return;
        }
        activeFormatter = chooseFormatter();
    }

    private static BookFormatter chooseFormatter() {
        if (Loader.isModLoaded("hextext")) {
            try {
                LOGGER.info("HexText detected – enabling HexText formatter integration.");
                return new HexTextFormatter();
            } catch (Throwable t) {
                LOGGER.warn("Failed to initialise HexText formatter, falling back to standalone behaviour.", t);
            }
        }
        return STANDALONE_FORMATTER;
    }

    public static BookFormatter getFormatter() {
        return activeFormatter;
    }

    public static void setFormatterForTesting(BookFormatter formatter) {
        if (formatter == null) {
            activeFormatter = STANDALONE_FORMATTER;
            testingOverride = false;
        } else {
            activeFormatter = formatter;
            testingOverride = true;
        }
    }

    public static void resetFormatterForTesting() {
        testingOverride = false;
        activeFormatter = STANDALONE_FORMATTER;
    }
}
