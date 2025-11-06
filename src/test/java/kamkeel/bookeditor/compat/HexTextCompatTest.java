package kamkeel.bookeditor.compat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import kamkeel.bookeditor.book.format.FormattingOptions;
import kamkeel.hextext.CommonProxy;
import kamkeel.hextext.HexText;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HexTextCompatTest {
    private CommonProxy originalProxy;

    @Before
    public void captureOriginalProxy() {
        originalProxy = HexText.getActiveProxy();
    }

    @After
    public void restoreOriginalProxy() {
        HexText.proxy = originalProxy;
    }

    @Test
    public void disabledIntegrationReturnsVanillaOptions() {
        FormattingOptions options = HexTextCompat.loadFormattingOptions(false);
        assertThat(options.ampersandSupport(), is(false));
        assertThat(options.htmlSupport(), is(false));
        assertThat(options.hexSectionSupport(), is(false));
    }

    @Test
    public void enabledIntegrationReflectsProxyConfiguration() {
        HexText.proxy = new StubProxy(true, false);
        FormattingOptions options = HexTextCompat.loadFormattingOptions(true);
        assertThat(options.ampersandSupport(), is(true));
        assertThat(options.htmlSupport(), is(false));
        assertThat(options.hexSectionSupport(), is(true));
    }

    @Test
    public void enabledIntegrationWithoutProxyFallsBackToDefaults() {
        HexText.proxy = null;
        FormattingOptions options = HexTextCompat.loadFormattingOptions(true);
        assertThat(options.ampersandSupport(), is(false));
        assertThat(options.htmlSupport(), is(false));
        assertThat(options.hexSectionSupport(), is(false));
    }

    private static final class StubProxy extends CommonProxy {
        private final boolean ampersand;
        private final boolean html;

        private StubProxy(boolean ampersand, boolean html) {
            this.ampersand = ampersand;
            this.html = html;
        }

        @Override
        public boolean allowUniversalAmpersand() {
            return ampersand;
        }

        @Override
        public boolean allowHtmlFormatting() {
            return html;
        }
    }
}
