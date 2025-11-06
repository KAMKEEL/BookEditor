package kamkeel.bookeditor.book;

import kamkeel.bookeditor.book.format.FormattingOptions;
import kamkeel.bookeditor.testutil.TestFontRenderer;
import kamkeel.bookeditor.util.FontRendererAccess;
import net.minecraft.client.gui.FontRenderer;
import org.junit.Before;

/**
 * Shared base class for tests that require a configured Minecraft font
 * renderer. The production code fetches FontRenderer through
 * Minecraft.getMinecraft(); these tests install a deterministic implementation
 * before each test runs.
 */
public abstract class AbstractBookTest {
    protected TestFontRenderer fontRenderer;

    @Before
    public void setUpFontRenderer() {
        FormattingOptions.resetDefaults();
        FontRendererAccess.reset();
        fontRenderer = new TestFontRenderer(6);
        FontRendererAccess.setProvider(() -> fontRenderer);
    }

    protected FontRenderer fontRenderer() {
        return fontRenderer;
    }
}
