package kamkeel.bookeditor.book;

import kamkeel.bookeditor.util.LineFormattingUtil;
import kamkeel.bookeditor.util.SimpleTextMetrics;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BookTextHelperTest {

    @Before
    public void setUpMetrics() {
        LineFormattingUtil.setMetrics(new SimpleTextMetrics());
    }

    private Book createBookWithSingleLine(String text) {
        Book book = new Book();
        Page page = new Page();
        page.lines.clear();
        Line line = new Line();
        line.text = text;
        page.lines.add(line);
        book.pages.add(page);
        book.cursorPage = 0;
        book.cursorLine = 0;
        book.cursorPosChars = 0;
        return book;
    }

    @Test
    public void removeCharDeletesFormattingCode() {
        Book book = createBookWithSingleLine("§aColor");
        book.cursorPosChars = 0;

        book.removeChar(true);

        assertThat(book.pages.get(0).lines.get(0).text, is("Color"));
    }

    @Test
    public void addTextAtCursorInsertsContent() {
        Book book = createBookWithSingleLine("Hello");
        book.cursorPosChars = 5;

        book.addTextAtCursor(" World");

        assertThat(book.pages.get(0).lines.get(0).text, is("Hello World"));
        assertThat(book.cursorPosChars, is(11));
    }

    @Test
    public void removeTextAcrossLinesJoinsSegments() {
        Book book = new Book();
        Page page = new Page();
        page.lines.clear();
        Line first = new Line();
        first.text = "Hello\n";
        Line second = new Line();
        second.text = "World";
        page.lines.add(first);
        page.lines.add(second);
        book.pages.add(page);
        book.cursorPage = 0;
        book.cursorLine = 1;
        book.cursorPosChars = 0;

        book.removeText(0, 0, 5, 0, 1, 0);

        assertThat(book.pages.get(0).lines.get(0).text, is("HelloWorld"));
    }
}
