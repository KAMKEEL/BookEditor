package kamkeel.bookeditor.book;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.util.concurrent.atomic.AtomicBoolean;

import kamkeel.bookeditor.book.format.FormattingOptions;
import kamkeel.bookeditor.testutil.TestFontRenderer;
import kamkeel.bookeditor.util.FontRendererAccess;
import net.minecraft.client.gui.FontRenderer;
import org.junit.Test;

/**
 * Unit tests covering the behaviour of {@link Line} without relying on the real
 * Minecraft runtime. These tests document the current wrapping rules so future
 * refactors can ensure parity.
 */
public class LineTest extends AbstractBookTest {

    @Test
    public void addTextSplitsOverflowAtWidthBoundary() {
        Line line = new Line();
        fontRenderer.setWidth('a', 6);

        String text = repeat('a', 30);
        String overflow = line.addText(0, text);

        // 116px / 6px per char = 19 characters on the first line
        assertThat(line.text.length(), is(19));
        assertThat(line.text, startsWith(repeat('a', 19)));
        assertThat(overflow, equalTo(repeat('a', 11)));
    }

    @Test
    public void addTextRespectsInsertionPosition() {
        Line line = new Line();
        line.text = "Hello";
        String overflow = line.addText(2, "--world");

        assertThat(line.text, equalTo("He--worldllo"));
        assertThat(overflow, equalTo(""));
    }

    @Test
    public void getActiveFormattingReturnsLastFormattingCodes() {
        Line line = new Line();
        line.text = "\u00a71Blue\u00a7lBold";

        assertThat(line.getActiveFormatting(), equalTo("\u00a71\u00a7l"));
    }

    @Test
    public void sizeStringToWidthInvokesFontRendererDirectly() {
        final FontRenderer original = fontRenderer();
        final AtomicBoolean calledViaPrivateHelper = new AtomicBoolean(false);
        final TestFontRenderer special = new TestFontRenderer(fontRenderer.getDefaultWidth()) {
            @Override
            public int sizeStringToWidth(String text, int maxWidth) {
                for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                    if (element.getClassName().contains("FontRendererPrivate")) {
                        calledViaPrivateHelper.set(true);
                    }
                }
                return 4;
            }
        };

        FontRendererAccess.setProvider(() -> special);
        try {
            int consumed = Line.sizeStringToWidth("abcdef", 100);
            assertThat(consumed, is(4));
            assertThat(calledViaPrivateHelper.get(), is(false));
        } finally {
            FontRendererAccess.setProvider(() -> original);
        }
    }

    @Test
    public void listFormattedStringToWidthWrapsAtWhitespaceBoundaries() {
        fontRenderer.setWidth('A', 10);
        fontRenderer.setWidth('B', 10);
        fontRenderer.setWidth('C', 10);
        fontRenderer.setWidth('D', 10);
        fontRenderer.setWidth(' ', 5);

        String text = "AAAA BBBB CCCC DDDD";
        FormattingOptions options = FormattingOptions.of(false, false, false);

        java.util.List<String> lines = Line.listFormattedStringToWidth(text, "", options);

        assertThat(lines.size(), is(2));
        assertThat(lines.get(0), equalTo("AAAA BBBB "));
        assertThat(lines.get(1), equalTo("CCCC DDDD"));
    }

    private static String repeat(char c, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(c);
        }
        return builder.toString();
    }
}
