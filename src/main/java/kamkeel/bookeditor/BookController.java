package kamkeel.bookeditor;

import cpw.mods.fml.common.Loader;
import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.HexTextBookFormatter;
import kamkeel.bookeditor.format.StandaloneBookFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

/**
 * Central place for selecting and exposing the active {@link BookFormatter} implementation.
 * During pre-initialisation we check whether HexText is present and, if so, wire up the
 * integration aware formatter. Tests can override the formatter via {@link #setFormatter(BookFormatter)}.
 */
public final class BookController {
    private static final Logger LOGGER = LogManager.getLogger(BookController.class);

    private static volatile BookFormatter formatter = new StandaloneBookFormatter();

    private BookController() {
    }

    public static void preInit() {
        formatter = selectFormatter();
    }

    public static BookFormatter getFormatter() {
        return formatter;
    }

    public static void setFormatter(BookFormatter newFormatter) {
        formatter = newFormatter != null ? newFormatter : new StandaloneBookFormatter();
    }

    private static BookFormatter selectFormatter() {
        if (isHexTextAvailable()) {
            try {
                Class<?> configClass = Class.forName("kamkeel.hextext.config.HexTextConfig");
                BooleanSupplier allowAmpersand = reflectiveBooleanSupplier(configClass, "isAmpersandAllowed");
                BooleanSupplier allowHtml = reflectiveBooleanSupplier(configClass, "isRgbHtmlFormatEnabled");
                return new HexTextBookFormatter(allowAmpersand, allowHtml);
            } catch (ReflectiveOperationException | LinkageError ex) {
                LOGGER.warn("Failed to initialise HexText integration, falling back to standalone formatter", ex);
            }
        }
        return new StandaloneBookFormatter();
    }

    private static boolean isHexTextAvailable() {
        if (!Loader.isModLoaded("hextext")) {
            return false;
        }
        try {
            Class.forName("kamkeel.hextext.HexText");
            return true;
        } catch (ClassNotFoundException ex) {
            LOGGER.debug("HexText reported as loaded but primary class was not found", ex);
            return false;
        }
    }

    private static BooleanSupplier reflectiveBooleanSupplier(Class<?> configClass, String methodName)
        throws ReflectiveOperationException {
        final Method method = configClass.getMethod(methodName);
        return () -> {
            try {
                Object result = method.invoke(null);
                return result instanceof Boolean && (Boolean) result;
            } catch (ReflectiveOperationException ex) {
                LOGGER.debug("Failed to query HexText config via {}", methodName, ex);
                return false;
            }
        };
    }
}
