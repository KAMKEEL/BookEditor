package kamkeel.bookeditor.format;

import kamkeel.bookeditor.BookController;
import kamkeel.hextext.config.HexTextConfig;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Helper used by parameterised tests to execute the same assertions against the
 * standalone formatter and the HexText powered formatter (with different
 * configuration states).
 */
public final class FormatterTestScenario {
    public enum Type {
        STANDALONE,
        HEX_TEXT
    }

    private final String displayName;
    private final Type type;
    private final Supplier<BookFormatter> supplier;
    private final Boolean ampersandEnabled;

    private BookFormatter currentFormatter;

    private FormatterTestScenario(String displayName, Type type, Supplier<BookFormatter> supplier, Boolean ampersandEnabled) {
        this.displayName = displayName;
        this.type = type;
        this.supplier = supplier;
        this.ampersandEnabled = ampersandEnabled;
    }

    public static FormatterTestScenario standalone() {
        return new FormatterTestScenario("Standalone", Type.STANDALONE, StandaloneBookFormatter::new, null);
    }

    public static FormatterTestScenario hexText(boolean ampersandEnabled) {
        return new FormatterTestScenario(
            ampersandEnabled ? "HexText (ampersand enabled)" : "HexText (ampersand disabled)",
            Type.HEX_TEXT,
            HexTextBookFormatter::new,
            ampersandEnabled
        );
    }

    public static Collection<Object[]> scenarios() {
        FormatterTestScenario standaloneScenario = standalone();
        FormatterTestScenario hexAmp = hexText(true);
        FormatterTestScenario hexNoAmp = hexText(false);
        return Arrays.asList(
            new Object[]{standaloneScenario.displayName, standaloneScenario},
            new Object[]{hexAmp.displayName, hexAmp},
            new Object[]{hexNoAmp.displayName, hexNoAmp}
        );
    }

    public void apply() {
        if (type == Type.HEX_TEXT) {
            HexTextConfig.resetToDefaults();
            if (ampersandEnabled != null) {
                HexTextConfig.setAllowAmpersand(ampersandEnabled);
            }
        }
        currentFormatter = supplier.get();
        BookController.setFormatter(currentFormatter);
    }

    public void reset() {
        if (type == Type.HEX_TEXT) {
            HexTextConfig.resetToDefaults();
        }
        BookController.setFormatter(new StandaloneBookFormatter());
        currentFormatter = null;
    }

    public boolean isHexText() {
        return type == Type.HEX_TEXT;
    }

    public boolean isAmpersandEnabled() {
        return Boolean.TRUE.equals(ampersandEnabled);
    }

    public BookFormatter getFormatter() {
        return currentFormatter;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
