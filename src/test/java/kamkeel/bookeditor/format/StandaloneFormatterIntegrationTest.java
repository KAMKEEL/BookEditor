package kamkeel.bookeditor.format;

import kamkeel.bookeditor.book.Book;
import kamkeel.bookeditor.book.Line;
import kamkeel.bookeditor.book.Page;
import kamkeel.bookeditor.controller.BookController;
import kamkeel.bookeditor.util.LineFormattingUtil;
import kamkeel.bookeditor.util.SimpleTextMetrics;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StandaloneFormatterIntegrationTest {

    @Before
    public void setUp() {
        BookController.setFormatterForTesting(StandaloneBookFormatter.INSTANCE);
        LineFormattingUtil.setMetrics(new SimpleTextMetrics());
    }

    @Test
    public void lineAddTextWrapsAndReturnsOverflow() {
        Line line = new Line();
        String overflow = line.addText(0, "§aThis line is long enough to overflow the default width of the book editor.");
        assertThat(overflow.isEmpty(), is(false));
        assertThat(LineFormattingUtil.getActiveFormatting(line.getTextWithWrappedFormatting()), is("§a"));
    }

    @Test
    public void pageAddTextSpillsIntoOverflow() {
        Page page = new Page();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            builder.append("segment ").append(i).append(' ');
        }
        String overflow = page.addText(0, 0, builder.toString());
        assertThat(page.lines.size() > 1, is(true));
        assertThat(overflow.isEmpty(), is(false));
    }

    @Test
    public void bookOperationsRespectFormattingWhenRemovingCodes() {
        Book book = new Book();
        book.addTextAtCursor("§aFormatted text");
        assertThat(book.getCurrLineWithWrappedFormatting(), containsString("§a"));

        book.cursorPosChars = 0;
        book.removeChar(true);
        assertThat(book.getCurrLine(), containsString("Formatted"));
        assertThat(book.getCurrLineWithWrappedFormatting().startsWith("§"), is(false));
    }
}
