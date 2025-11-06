package kamkeel.bookeditor.book;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class BookTest extends AbstractBookTest {

    @Test
    public void addTextAtCursorAppendsToFirstPage() {
        Book book = new Book();
        book.pages.add(new Page());

        book.addTextAtCursor("Hello world");

        assertThat(book.totalPages(), is(1));
        assertThat(book.pages.get(0).lines.get(0).text, equalTo("Hello world"));
        assertThat(book.cursorPage, is(0));
        assertThat(book.cursorLine, is(0));
        assertThat(book.cursorPosChars, is("Hello world".length()));
    }

    @Test
    public void addTextBeyondFirstLineCreatesAdditionalLines() {
        Book book = new Book();
        book.pages.add(new Page());
        String longText = repeat('x', 150);

        book.addTextAtCursor(longText);

        assertThat(book.pages.get(0).lines.size() > 1, is(true));
        assertThat(book.cursorLine >= 1, is(true));
        assertThat(book.cursorPage, is(0));
    }

    @Test
    public void backspaceDeletesFormattingCodeAsSingleUnit() {
        Book book = new Book();
        Page page = new Page();
        page.lines.get(0).text = "\u00a71";
        book.pages.add(page);

        book.cursorPage = 0;
        book.cursorLine = 0;
        book.cursorPosChars = page.lines.get(0).text.length();

        book.removeChar(false);

        assertThat(page.lines.get(0).text, equalTo(""));
        assertThat(book.cursorPosChars, is(0));
    }

    @Test
    public void deleteAheadSkipsFormattingSequences() {
        Book book = new Book();
        Page page = new Page();
        page.lines.get(0).text = "Test\u00a71More";
        book.pages.add(page);

        book.cursorPage = 0;
        book.cursorLine = 0;
        book.cursorPosChars = 4; // position after "Test"

        book.removeChar(true);

        assertThat(page.lines.get(0).text, equalTo("TestMore"));
    }

    @Test
    public void enterCreatesNewLineWithoutSecondKeypress() {
        Book book = new Book();
        book.pages.add(new Page());

        book.addTextAtCursor("Hello");
        book.addTextAtCursor("\n");

        Page page = book.pages.get(0);

        assertThat(page.lines.size(), greaterThan(1));
        assertThat(page.lines.get(0).text, equalTo("Hello\n"));
        assertThat(book.cursorPage, is(0));
        assertThat(book.cursorLine, is(1));
        assertThat(book.cursorPosChars, is(0));
    }

    @Test
    public void backspaceAtStartOfPageDeletesPreviousPageContent() {
        Book book = new Book();

        Page first = new Page();
        first.lines.get(0).text = "A";
        Page second = new Page();
        second.lines.get(0).text = "B";
        book.pages.add(first);
        book.pages.add(second);

        book.cursorPage = 1;
        book.cursorLine = 0;
        book.cursorPosChars = 0;

        book.removeChar(false);

        assertThat(book.cursorPage, is(0));
        assertThat(book.cursorLine, is(first.lines.size() - 1));
        assertThat(first.lines.get(first.lines.size() - 1).text, equalTo("B"));
        assertThat(book.totalPages(), is(1));
    }

    private static String repeat(char c, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(c);
        }
        return builder.toString();
    }
}
