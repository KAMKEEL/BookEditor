package kamkeel.bookeditor.book.format;

import java.util.Objects;

/**
 * Immutable snapshot describing which formatting syntaxes should be recognised
 * while editing a book. The options are derived from the runtime environment
 * (Hex Text integration, configuration files, etc.) so helper classes can adapt
 * their behaviour without hard coding mod specific checks.
 */
public final class FormattingOptions {
    private final boolean ampersandSupport;
    private final boolean htmlSupport;
    private final boolean hexSectionSupport;
    private static volatile FormattingOptions cachedDefaults;

    private FormattingOptions(boolean ampersandSupport, boolean htmlSupport, boolean hexSectionSupport) {
        this.ampersandSupport = ampersandSupport;
        this.htmlSupport = htmlSupport;
        this.hexSectionSupport = hexSectionSupport;
    }

    public static FormattingOptions defaults() {
        FormattingOptions options = cachedDefaults;
        if (options == null) {
            options = HexTextIntegration.detectOptions();
            cachedDefaults = options;
        }
        return options;
    }

    public static void overrideDefaults(FormattingOptions override) {
        cachedDefaults = override;
    }

    public static void resetDefaults() {
        cachedDefaults = null;
    }

    public static FormattingOptions of(boolean ampersandSupport, boolean htmlSupport, boolean hexSectionSupport) {
        return new FormattingOptions(ampersandSupport, htmlSupport, hexSectionSupport);
    }

    public boolean ampersandSupport() {
        return ampersandSupport;
    }

    public boolean htmlSupport() {
        return htmlSupport;
    }

    public boolean hexSectionSupport() {
        return hexSectionSupport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FormattingOptions)) {
            return false;
        }
        FormattingOptions other = (FormattingOptions) o;
        return ampersandSupport == other.ampersandSupport
            && htmlSupport == other.htmlSupport
            && hexSectionSupport == other.hexSectionSupport;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ampersandSupport, htmlSupport, hexSectionSupport);
    }

    @Override
    public String toString() {
        return "FormattingOptions{"
            + "ampersandSupport=" + ampersandSupport
            + ", htmlSupport=" + htmlSupport
            + ", hexSectionSupport=" + hexSectionSupport
            + '}';
    }
}
