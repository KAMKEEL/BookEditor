package kamkeel.bookeditor.compat;

import kamkeel.bookeditor.book.format.FormattingOptions;
import kamkeel.hextext.CommonProxy;
import kamkeel.hextext.HexText;

/**
 * Lightweight bridge into the optional Hex Text mod. All calls are guarded by
 * the detected mod presence so the editor can safely compile against the Hex
 * Text API while still working when the dependency is absent at runtime.
 */
public final class HexTextCompat {
    private HexTextCompat() {
    }

    public static FormattingOptions loadFormattingOptions(boolean hexTextEnabled) {
        if (!hexTextEnabled) {
            return FormattingOptions.of(false, false, false);
        }
        try {
            CommonProxy proxy = HexText.getActiveProxy();
            if (proxy == null) {
                return FormattingOptions.of(false, false, false);
            }
            boolean ampersand = proxy.allowUniversalAmpersand();
            boolean html = proxy.allowHtmlFormatting();
            return FormattingOptions.of(ampersand, html, true);
        } catch (Throwable ignored) {
            return FormattingOptions.of(false, false, false);
        }
    }
}
