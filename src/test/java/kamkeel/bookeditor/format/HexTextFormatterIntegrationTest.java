package kamkeel.bookeditor.format;

import kamkeel.bookeditor.book.Book;
import kamkeel.bookeditor.book.Line;
import kamkeel.bookeditor.book.Page;
import kamkeel.bookeditor.controller.BookController;
import kamkeel.bookeditor.util.FormattingUtil;
import kamkeel.bookeditor.util.LineFormattingUtil;
import kamkeel.bookeditor.util.SimpleTextMetrics;
import kamkeel.hextext.config.HexTextConfig;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HexTextFormatterIntegrationTest {

    @Before
    public void setUp() {
        HexTextBookFormatter.ensureProxy();
        HexTextConfig.resetToDefaults();
        BookController.setFormatterForTesting(new HexTextBookFormatter());
        LineFormattingUtil.setMetrics(new SimpleTextMetrics());
    }

    @Test
    public void detectFormattingCodeLengthSupportsHexColors() {
        assertThat(FormattingUtil.detectFormattingCodeLength("§#ffaa00Hello", 0), is(8));
    }

    @Test
    public void ampersandDetectionRespectsConfiguration() {
        HexTextConfig.setAllowAmpersand(false);
        assertThat(FormattingUtil.detectFormattingCodeLength("&aHello", 0), is(0));

        HexTextConfig.setAllowAmpersand(true);
        assertThat(FormattingUtil.detectFormattingCodeLength("&aHello", 0), is(2));
    }

    @Test
    public void lineWrappingPreservesHexFormattingAcrossSegments() {
        Line line = new Line();
        String overflow = line.addText(0, "§#ff00aaThis line includes a long amount of text so that we can validate wrapping.");
        assertThat("overflow=" + overflow, overflow.isEmpty(), is(false));
        String activeFormatting = LineFormattingUtil.getActiveFormatting(line.getTextWithWrappedFormatting());
        assertThat(activeFormatting, containsString("#ff00aa"));
        assertThat(LineFormattingUtil.getActiveFormatting(activeFormatting + overflow), containsString("#ff00aa"));
    }

    @Test
    public void bookRemovingNextCharRemovesWholeHexCode() {
        Book book = new Book();
        book.addTextAtCursor("§#00ff00Vivid words");
        assertThat(book.getCurrLineWithWrappedFormatting(), containsString("#00ff00"));

        book.cursorPosChars = 0;
        book.removeChar(true);
        assertThat(book.getCurrLine().startsWith("Vivid"), is(true));
        assertThat(book.getCurrLine().contains("§"), is(false));
    }

    @Test
    public void pageWrappingWithHexCodesProducesOverflow() {
        Page page = new Page();
        StringBuilder builder = new StringBuilder("§#123456");
        for (int i = 0; i < 40; i++) {
            builder.append("hex segment ").append(i).append(' ');
        }
        String overflow = page.addText(0, 0, builder.toString());
        assertThat(page.lines.size() > 1, is(true));
        String nextFormatting = page.lines.get(1).wrappedFormatting;
        assertThat(nextFormatting.toLowerCase(), containsString("#123456"));
    }
}
