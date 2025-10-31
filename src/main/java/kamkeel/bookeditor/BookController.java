package kamkeel.bookeditor;

import cpw.mods.fml.common.Loader;
import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.HexTextBookFormatter;
import kamkeel.bookeditor.format.StandaloneBookFormatter;
import kamkeel.bookeditor.util.LineFormattingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Central manager responsible for deciding which formatter implementation is used
 * across the mod. The controller inspects whether optional integrations are
 * available (such as HexText) and exposes the selected {@link BookFormatter}
 * instance to the rest of the code base.
 */
public final class BookController {
    private static final Logger LOGGER = LogManager.getLogger("BookEditor");

    private static volatile BookFormatter formatter = new StandaloneBookFormatter();

    private BookController() {
    }

    /**
     * Performs the detection logic during pre-initialisation and selects the
     * most capable formatter available in the current environment.
     */
    public static void preInit() {
        setFormatter(selectFormatter());
    }

    /**
     * Returns the formatter currently used by the editor.
     */
    public static BookFormatter getFormatter() {
        return formatter;
    }

    /**
     * Allows tests to override the active formatter. Passing {@code null} will
     * reset the system back to the standalone formatter.
     */
    public static synchronized void setFormatter(BookFormatter newFormatter) {
        if (newFormatter == null) {
            newFormatter = new StandaloneBookFormatter();
        }
        formatter = newFormatter;
        LineFormattingUtil.setFormatter(newFormatter);
        LOGGER.debug("Using {} for book formatting", newFormatter.getDisplayName());
    }

    private static BookFormatter selectFormatter() {
        if (Loader.isModLoaded("hextext")) {
            try {
                return new HexTextBookFormatter();
            } catch (Throwable t) {
                LOGGER.warn("Failed to initialise HexText integration, falling back to standalone formatter", t);
            }
        }
        return new StandaloneBookFormatter();
    }
}
