package kamkeel.bookeditor.book.format;

import cpw.mods.fml.common.Loader;

/**
 * Handles optional integration with the Hex Text mod. All interaction is kept
 * behind defensive reflection to avoid {@link NoClassDefFoundError}s when the
 * dependency is not present.
 */
public final class HexTextIntegration {
    private static final String MOD_ID = "hextext";
    private static final String HEX_TEXT_CLASS = "kamkeel.hextext.HexText";

    private HexTextIntegration() {
    }

    public static FormattingOptions detectOptions() {
        boolean ampersand = false;
        boolean html = false;
        boolean hexSection = false;

        try {
            if (Loader.isModLoaded(MOD_ID)) {
                Object proxy = resolveProxy();
                if (proxy != null) {
                    ampersand = invokeBoolean(proxy, "allowUniversalAmpersand")
                        || invokeBoolean(proxy, "convertAmpersandsOnSigns")
                        || invokeBoolean(proxy, "convertAmpersandsInChat")
                        || invokeBoolean(proxy, "convertAmpersandsInRepairs");
                    html = invokeBoolean(proxy, "allowHtmlFormatting");
                    hexSection = true; // Hex Text always understands section based hex codes when present
                } else {
                    ampersand = true;
                    html = true;
                    hexSection = true;
                }
            }
        } catch (Throwable ignored) {
            // Running outside of an FML environment (e.g. unit tests). Treat Hex Text as unavailable.
        }

        return FormattingOptions.of(ampersand, html, hexSection);
    }

    private static Object resolveProxy() {
        try {
            Class<?> hexTextClass = Class.forName(HEX_TEXT_CLASS);
            return hexTextClass.getMethod("getActiveProxy").invoke(null);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static boolean invokeBoolean(Object target, String method) {
        try {
            return (Boolean) target.getClass().getMethod(method).invoke(target);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }
}
