package kamkeel.bookeditor.book;

import kamkeel.bookeditor.util.LineFormattingUtil;
import kamkeel.bookeditor.util.SimpleTextMetrics;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BookCursorHelperTest {

    @Before
    public void setUpMetrics() {
        LineFormattingUtil.setMetrics(new SimpleTextMetrics());
    }

    private Book createTwoLineBook() {
        Book book = new Book();
        Page page = new Page();
        page.lines.clear();
        Line first = new Line();
        first.text = "First line\n";
        Line second = new Line();
        second.text = "Second";
        page.lines.add(first);
        page.lines.add(second);
        book.pages.add(page);
        book.cursorPage = 0;
        book.cursorLine = 1;
        book.cursorPosChars = 0;
        return book;
    }

    @Test
    public void movingCursorUpGoesToPreviousLine() {
        Book book = createTwoLineBook();

        book.moveCursor(Book.CursorDirection.UP);

        assertThat(book.cursorLine, is(0));
    }

    @Test
    public void movingCursorDownStaysWithinBounds() {
        Book book = createTwoLineBook();
        book.cursorLine = 0;

        book.moveCursor(Book.CursorDirection.DOWN);

        assertThat(book.cursorLine, is(1));
    }
}
