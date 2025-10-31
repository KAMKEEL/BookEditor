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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public class BookCursorHelperTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        return FormatterTestScenario.scenarios();
    }

    @Parameterized.Parameter(0)
    public String name;

    @Parameterized.Parameter(1)
    public FormatterTestScenario scenario;

    @Before
    public void setUpMetrics() {
        scenario.apply();
        LineFormattingUtil.setMetrics(new SimpleTextMetrics());
    }

    @After
    public void tearDown() {
        scenario.reset();
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

    private Book createMultiPageBook() {
        Book book = new Book();
        Page firstPage = new Page();
        firstPage.lines.clear();
        Line firstTop = new Line();
        firstTop.text = "Page0-Line0\n";
        Line firstBottom = new Line();
        firstBottom.text = "Page0-Line1";
        firstPage.lines.add(firstTop);
        firstPage.lines.add(firstBottom);

        Page secondPage = new Page();
        secondPage.lines.clear();
        Line secondTop = new Line();
        secondTop.text = "Page1-Line0\n";
        Line secondBottom = new Line();
        secondBottom.text = "Page1-Line1";
        secondPage.lines.add(secondTop);
        secondPage.lines.add(secondBottom);

        book.pages.add(firstPage);
        book.pages.add(secondPage);
        book.cursorPage = 1;
        book.cursorLine = 0;
        book.cursorPosChars = 5;
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

    @Test
    public void movingCursorUpAtDocumentStartClampsToFirstChar() {
        Book book = createTwoLineBook();
        book.cursorLine = 0;
        book.cursorPosChars = 4;

        book.moveCursor(Book.CursorDirection.UP);

        assertThat(book.cursorLine, is(0));
        assertThat(book.cursorPosChars, is(0));
        assertThat(book.cursorPage, is(0));
    }

    @Test
    public void movingCursorUpAcrossPagesMovesToPreviousPageEnd() {
        Book book = createMultiPageBook();

        book.moveCursor(Book.CursorDirection.UP);

        assertThat(book.cursorPage, is(0));
        assertThat(book.cursorLine, is(book.pages.get(0).lines.size() - 1));
        assertThat(book.cursorPosChars, is(5));
    }

    @Test
    public void movingCursorDownAcrossPagesMovesToNextPageStart() {
        Book book = createMultiPageBook();
        book.cursorPage = 0;
        book.cursorLine = 1;
        book.cursorPosChars = 11;

        book.moveCursor(Book.CursorDirection.DOWN);

        assertThat(book.cursorPage, is(1));
        assertThat(book.cursorLine, is(0));
        assertThat(book.cursorPosChars, is(11));
    }

    @Test
    public void movingCursorRightSkipsFormattingCodes() {
        Book book = createTwoLineBook();
        Line formatted = new Line();
        formatted.text = "\u00a7aB";
        book.pages.get(0).lines.set(1, formatted);
        book.cursorLine = 1;
        book.cursorPosChars = 0;

        book.moveCursor(Book.CursorDirection.RIGHT);

        assertThat(book.cursorPosChars, is(formatted.text.length()));
    }

    @Test
    public void movingCursorRightSkipsAmpersandCodesWhenEnabled() {
        assumeTrue(scenario.isHexText() && scenario.isAmpersandEnabled());
        Book book = createTwoLineBook();
        Line formatted = new Line();
        formatted.text = "&aB";
        book.pages.get(0).lines.set(1, formatted);
        book.cursorLine = 1;
        book.cursorPosChars = 0;

        book.moveCursor(Book.CursorDirection.RIGHT);

        assertThat(book.cursorPosChars, is(formatted.text.length()));
    }

    @Test
    public void movingCursorRightTreatsAmpersandAsLiteralWhenDisabled() {
        assumeTrue(scenario.isHexText() && !scenario.isAmpersandEnabled());
        Book book = createTwoLineBook();
        Line formatted = new Line();
        formatted.text = "&aB";
        book.pages.get(0).lines.set(1, formatted);
        book.cursorLine = 1;
        book.cursorPosChars = 0;

        book.moveCursor(Book.CursorDirection.RIGHT);

        assertThat(book.cursorPosChars, is(1));
    }

    @Test
    public void movingCursorLeftSkipsFormattingCodes() {
        Book book = createTwoLineBook();
        Line formatted = new Line();
        formatted.text = "\u00a7aB";
        book.pages.get(0).lines.set(1, formatted);
        book.cursorLine = 1;
        book.cursorPosChars = formatted.text.length();

        book.moveCursor(Book.CursorDirection.LEFT);
        assertThat(book.cursorPosChars, is(2));

        book.moveCursor(Book.CursorDirection.LEFT);
        assertThat(book.cursorLine, is(0));
        assertThat(book.cursorPosChars, is(book.pages.get(0).lines.get(0).text.length() - 1));
    }

    @Test
    public void movingCursorRightAcrossNewlineMovesToNextLineStart() {
        Book book = createTwoLineBook();
        book.cursorLine = 0;
        book.cursorPosChars = book.pages.get(0).lines.get(0).text.length() - 1;

        book.moveCursor(Book.CursorDirection.RIGHT);

        assertThat(book.cursorLine, is(1));
        assertThat(book.cursorPosChars, is(0));
    }

    @Test
    public void placingCursorFromPixelMovesToRequestedColumn() {
        Book book = createTwoLineBook();
        Line line = book.pages.get(0).lines.get(1);
        int pixelOffset = LineFormattingUtil.getMetrics().stringWidth(line.getTextWithWrappedFormatting());

        BookCursorHelper.placeCursorFromPixel(book, 1, pixelOffset);

        assertThat(book.cursorLine, is(1));
        assertThat(book.cursorPosChars, is(line.text.length()));
    }

    @Test
    public void placingCursorFromPixelSkipsFormattingCodes() {
        Book book = createTwoLineBook();
        Line formatted = new Line();
        formatted.text = "\u00a7aBold";
        book.pages.get(0).lines.set(1, formatted);
        int pixelOffset = LineFormattingUtil.getMetrics().stringWidth(formatted.getTextWithWrappedFormatting());

        BookCursorHelper.placeCursorFromPixel(book, 1, pixelOffset);

        assertThat(book.cursorPosChars, is(formatted.text.length()));
    }

    @Test
    public void placingCursorFromPixelSkipsAmpersandCodesWhenEnabled() {
        assumeTrue(scenario.isHexText() && scenario.isAmpersandEnabled());
        Book book = createTwoLineBook();
        Line formatted = new Line();
        formatted.text = "&aBold";
        book.pages.get(0).lines.set(1, formatted);
        int pixelOffset = LineFormattingUtil.getMetrics().stringWidth(formatted.getTextWithWrappedFormatting());

        BookCursorHelper.placeCursorFromPixel(book, 1, pixelOffset);

        assertThat(book.cursorPosChars, is(formatted.text.length()));
    }

    @Test
    public void placingCursorFromPixelTreatsAmpersandAsLiteralWhenDisabled() {
        assumeTrue(scenario.isHexText() && !scenario.isAmpersandEnabled());
        Book book = createTwoLineBook();
        Line formatted = new Line();
        formatted.text = "&aBold";
        book.pages.get(0).lines.set(1, formatted);
        int singleCharWidth = LineFormattingUtil.getMetrics().stringWidth("&");

        BookCursorHelper.placeCursorFromPixel(book, 1, singleCharWidth);

        assertThat(book.cursorPosChars, is(1));
    }

    @Test
    public void placingCursorFromPixelIgnoresTrailingNewline() {
        Book book = createTwoLineBook();
        Line withNewline = new Line();
        withNewline.text = "Row\n";
        book.pages.get(0).lines.set(1, withNewline);
        int pixelOffset = LineFormattingUtil.getMetrics().stringWidth(withNewline.getTextWithWrappedFormatting());

        BookCursorHelper.placeCursorFromPixel(book, 1, pixelOffset);

        assertThat(book.cursorPosChars, is(withNewline.text.length() - 1));
    }

    @Test
    public void movingCursorUpThroughBlankLinesStaysOnEachLine() {
        Book book = new Book();
        Page page = new Page();
        page.lines.clear();
        Line first = new Line();
        first.text = "First\n";
        Line blank = new Line();
        blank.text = "\n";
        Line blank2 = new Line();
        blank2.text = "\n";
        Line last = new Line();
        last.text = "Last";
        page.lines.add(first);
        page.lines.add(blank);
        page.lines.add(blank2);
        page.lines.add(last);
        book.pages.add(page);
        book.cursorLine = 3;
        book.cursorPosChars = 0;

        book.moveCursor(Book.CursorDirection.UP);
        assertThat(book.cursorLine, is(2));
        assertThat(book.cursorPosChars, is(0));

        book.moveCursor(Book.CursorDirection.UP);
        assertThat(book.cursorLine, is(1));
        assertThat(book.cursorPosChars, is(0));
    }

    @Test
    public void movingCursorDownThroughBlankLinesStaysOnEachLine() {
        Book book = new Book();
        Page page = new Page();
        page.lines.clear();
        Line first = new Line();
        first.text = "First\n";
        Line blank = new Line();
        blank.text = "\n";
        Line blank2 = new Line();
        blank2.text = "\n";
        Line last = new Line();
        last.text = "Last";
        page.lines.add(first);
        page.lines.add(blank);
        page.lines.add(blank2);
        page.lines.add(last);
        book.pages.add(page);
        book.cursorLine = 0;
        book.cursorPosChars = first.text.length() - 1;

        book.moveCursor(Book.CursorDirection.DOWN);
        assertThat(book.cursorLine, is(1));
        assertThat(book.cursorPosChars, is(0));

        book.moveCursor(Book.CursorDirection.DOWN);
        assertThat(book.cursorLine, is(2));
        assertThat(book.cursorPosChars, is(0));
    }
}
