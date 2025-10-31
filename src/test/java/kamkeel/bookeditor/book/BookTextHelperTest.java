package kamkeel.bookeditor.book;

import kamkeel.bookeditor.format.FormatterTestScenario;
import kamkeel.bookeditor.util.LineFormattingUtil;
import kamkeel.bookeditor.util.SimpleTextMetrics;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public class BookTextHelperTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        return FormatterTestScenario.scenarios();
    }

    @Parameterized.Parameter(0)
    public String name;

    @Parameterized.Parameter(1)
    public FormatterTestScenario scenario;

    @Before
    public void setUp() {
        scenario.apply();
        LineFormattingUtil.setMetrics(new SimpleTextMetrics());
    }

    @After
    public void tearDown() {
        scenario.reset();
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

    private Page createPage(String... lines) {
        Page page = new Page();
        page.lines.clear();
        for (String text : lines) {
            Line line = new Line();
            line.text = text;
            page.lines.add(line);
        }
        return page;
    }

    private Book createBook(Page... pages) {
        Book book = new Book();
        Collections.addAll(book.pages, pages);
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
    public void removeCharDeletesAmpersandFormattingWhenEnabled() {
        assumeTrue(scenario.isHexText() && scenario.isAmpersandEnabled());
        Book book = createBookWithSingleLine("&aColor");
        book.cursorPosChars = 0;

        book.removeChar(true);

        assertThat(book.pages.get(0).lines.get(0).text, is("Color"));
    }

    @Test
    public void removeCharTreatsAmpersandAsLiteralWhenDisabled() {
        assumeTrue(scenario.isHexText() && !scenario.isAmpersandEnabled());
        Book book = createBookWithSingleLine("&aColor");
        book.cursorPosChars = 0;

        book.removeChar(true);

        assertThat(book.pages.get(0).lines.get(0).text, is("aColor"));
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

    @Test
    public void removeCharBackspaceMergesWithPreviousLine() {
        Page page = createPage("Hello\n", "World");
        Book book = createBook(page);
        book.cursorLine = 1;
        book.cursorPosChars = 0;

        book.removeChar(false);

        assertThat(book.pages.get(0).lines.get(0).text, is("HelloWorld"));
        assertThat(book.pages.get(0).lines.size(), is(1));
        assertThat(book.cursorLine, is(0));
        assertThat(book.cursorPosChars, is(5));
    }

    @Test
    public void removeCharDeleteMergesWithNextLine() {
        Page page = createPage("Hello\n", "World");
        Book book = createBook(page);
        book.cursorLine = 0;
        book.cursorPosChars = 5;

        book.removeChar(true);

        assertThat(book.pages.get(0).lines.get(0).text, is("HelloWorld"));
        assertThat(book.pages.get(0).lines.size(), is(1));
        assertThat(book.cursorLine, is(0));
        assertThat(book.cursorPosChars, is(5));
    }

    @Test
    public void removeCharDeleteRemovesEmptyNextPage() {
        Page first = createPage("First");
        Page empty = createPage("");
        Book book = createBook(first, empty);
        book.cursorLine = 0;
        book.cursorPosChars = 5;

        book.removeChar(true);

        assertThat(book.pages.size(), is(1));
        assertThat(book.pages.get(0).lines.get(0).text, is("First"));
    }

    @Test
    public void addTextAtCursorRespectsNewlines() {
        Book book = createBook(createPage(""));

        book.addTextAtCursor("Line1\nLine2");

        String lineText = book.pages.get(0).lines.get(0).text;
        assertThat(lineText.startsWith("Line1"), is(true));
        assertThat(lineText.contains("\n"), is(true));
    }

    @Test
    public void addTextOverflowsToNewPage() {
        Book book = createBook(createPage(""));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            builder.append('a');
        }

        book.addTextAtCursor(builder.toString());

        assertThat(book.pages.size() >= 2, is(true));
        assertThat(book.cursorPage, is(book.pages.size() - 1));
        assertThat(book.cursorPosChars > 0, is(true));
    }

    @Test
    public void removeTextAcrossPagesBringsLaterContentForward() {
        Page first = createPage("Hello\n", "World");
        Page second = createPage("SecondPage");
        Book book = createBook(first, second);

        book.removeText(0, 0, 0, 1, 0, 6);

        String combined = book.pages.get(0).asString();
        assertThat(combined, is("Page"));
        assertThat(book.pages.size(), is(1));
    }

    @Test
    public void addTextMaintainsFormattingPrefix() {
        Book book = createBookWithSingleLine("\u00a7aColor");
        book.cursorPosChars = book.pages.get(0).lines.get(0).text.length();

        book.addTextAtCursor("ful");

        assertThat(book.pages.get(0).lines.get(0).text, is("\u00a7aColorful"));
    }

    @Test
    public void addTextAtCursorDoubleNewlineMovesCursorForward() {
        Book book = createBook(createPage(""));

        book.addTextAtCursor("\n");

        assertThat(book.cursorLine, is(1));
        assertThat(book.cursorPosChars, is(0));
        assertThat(book.pages.get(0).lines.size() >= 2, is(true));

        book.addTextAtCursor("\n");

        assertThat(book.cursorLine, is(2));
        assertThat(book.cursorPosChars, is(0));
        assertThat(book.pages.get(0).lines.size() >= 3, is(true));
    }

    @Test
    public void addTextAtCursorNewlineAtFullPageCreatesNewPage() {
        Page page = new Page();
        page.lines.clear();
        for (int i = 0; i < 12; i++) {
            Line line = new Line();
            line.text = "Line" + i + "\n";
            page.lines.add(line);
        }
        Line last = new Line();
        last.text = "";
        page.lines.add(last);

        Book book = createBook(page);
        book.cursorPage = 0;
        book.cursorLine = page.lines.size() - 1;
        book.cursorPosChars = 0;

        book.addTextAtCursor("\n");

        assertThat(book.cursorPage, is(1));
        assertThat(book.cursorLine, is(0));
        assertThat(book.cursorPosChars, is(0));
        assertThat(book.pages.size(), is(2));
    }

    @Test
    public void addTextAtCursorNewlineWithFormattingMovesCursorForward() {
        Book book = createBook(createPage(""));

        book.addTextAtCursor("\u00a7a\n");

        assertThat(book.cursorLine, is(1));
        assertThat(book.cursorPosChars, is(0));
    }

    @Test
    public void addTextAtCursorNewlineWithAmpersandFormattingMovesCursorForwardWhenEnabled() {
        assumeTrue(scenario.isHexText() && scenario.isAmpersandEnabled());
        Book book = createBook(createPage(""));

        book.addTextAtCursor("&a\n");

        assertThat(book.cursorLine, is(1));
        assertThat(book.cursorPosChars, is(0));
    }
}
