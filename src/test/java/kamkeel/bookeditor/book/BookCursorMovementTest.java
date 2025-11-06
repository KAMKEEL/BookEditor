package kamkeel.bookeditor.book;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class BookCursorMovementTest extends AbstractBookTest {

    @Test
    public void moveCursorUpAlignsByPixelWidth() {
        Book book = new Book();
        Page page = new Page();
        page.lines.get(0).text = "Short";
        Line second = new Line();
        second.wrappedFormatting = page.lines.get(0).getActiveFormatting();
        second.text = "A much longer line of text";
        page.lines.add(second);
        book.pages.add(page);

        book.cursorPage = 0;
        book.cursorLine = 1;
        book.cursorPosChars = second.text.length();

        book.moveCursor(Book.CursorDirection.UP);

        assertThat(book.cursorLine, is(0));
        assertThat(book.cursorPage, is(0));
        // Cursor should land at the end of the first line
        assertThat(book.cursorPosChars, equalTo(page.lines.get(0).text.length()));
    }

    @Test
    public void moveCursorDownSkipsFormattingCodes() {
        Book book = new Book();
        Page page = new Page();
        page.lines.get(0).text = "Line\u00a71";
        Line second = new Line();
        second.wrappedFormatting = page.lines.get(0).getActiveFormatting();
        second.text = "Next";
        page.lines.add(second);
        book.pages.add(page);

        book.cursorPage = 0;
        book.cursorLine = 0;
        book.cursorPosChars = page.lines.get(0).text.length();

        book.moveCursor(Book.CursorDirection.DOWN);

        assertThat(book.cursorLine, is(1));
        assertThat(book.cursorPosChars <= second.text.length(), is(true));
    }
}
