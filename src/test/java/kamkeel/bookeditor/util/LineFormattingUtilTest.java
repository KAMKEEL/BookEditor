package kamkeel.bookeditor.util;

import kamkeel.bookeditor.book.BookController;
import kamkeel.bookeditor.book.Line;
import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.HexTextBookFormatter;
import kamkeel.bookeditor.format.StandaloneBookFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class LineFormattingUtilTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"Standalone", new StandaloneBookFormatter()},
            {"HexText", new HexTextBookFormatter()}
        });
    }

    private final BookFormatter formatter;

    public LineFormattingUtilTest(String name, BookFormatter formatter) {
        this.formatter = formatter;
    }

    @Before
    public void setUpMetrics() {
        BookController.overrideFormatterForTesting(formatter);
        LineFormattingUtil.setMetrics(new SimpleTextMetrics());
    }

    @Test
    public void wrapStringToWidthBreaksOnWhitespace() {
        String wrapped = LineFormattingUtil.wrapStringToWidth("This is a very long line", LineFormattingUtil.BOOK_TEXT_WIDTH, "");
        String[] segments = wrapped.split(String.valueOf(Line.SPLIT_CHAR));
        assertThat(segments.length > 1, is(true));
        assertThat(segments[0].endsWith(" "), is(true));
    }

    @Test
    public void listFormattedStringToWidthMaintainsFormatting() {
        List<String> segments = LineFormattingUtil.listFormattedStringToWidth("§aColoured text continues", "");
        assertThat(segments.get(0).startsWith("§a"), is(true));
        assertThat(LineFormattingUtil.getActiveFormatting(segments.get(0)), is("§a"));
    }

    @Test
    public void wrapStringToWidthPrefersEarlierWhitespaceWhenAvailable() {
        int wrapWidth = LineFormattingUtil.getMetrics().stringWidth("WordOne Word");
        String wrapped = LineFormattingUtil.wrapStringToWidth("WordOne WordTwo", wrapWidth, "");
        String[] segments = wrapped.split(String.valueOf(Line.SPLIT_CHAR));
        assertThat(segments[0], is("WordOne "));
        assertThat(segments[1], is("WordTwo"));
    }

    @Test
    public void wrapStringToWidthRespectsExplicitNewlines() {
        String wrapped = LineFormattingUtil.wrapStringToWidth("First line\nSecond line", LineFormattingUtil.BOOK_TEXT_WIDTH, "");
        String[] segments = wrapped.split(String.valueOf(Line.SPLIT_CHAR));
        assertThat(segments[0], is("First line\n"));
        assertThat(segments[1], is("Second line"));
    }

    @Test
    public void wrapStringToWidthKeepsWhitespaceAtBoundary() {
        int wrapWidth = LineFormattingUtil.getMetrics().stringWidth("ABCDEFGHIJ");
        String wrapped = LineFormattingUtil.wrapStringToWidth("ABCDEFGHIJ K", wrapWidth, "");
        String[] segments = wrapped.split(String.valueOf(Line.SPLIT_CHAR));
        assertThat(segments[0], is("ABCDEFGHIJ "));
        assertThat(segments[1], is("K"));
    }

    @Test
    public void wrapStringToWidthRetainsNewlineAtBoundary() {
        int wrapWidth = LineFormattingUtil.getMetrics().stringWidth("Hello");
        String wrapped = LineFormattingUtil.wrapStringToWidth("Hello\nWorld", wrapWidth, "");
        String[] segments = wrapped.split(String.valueOf(Line.SPLIT_CHAR));
        assertThat(segments[0], is("Hello\n"));
        assertThat(segments[1], is("World"));
    }

    @Test
    public void getActiveFormattingAccumulatesStyles() {
        String formatted = "§aGreen §lBold";
        assertThat(LineFormattingUtil.getActiveFormatting(formatted), is("§a§l"));
    }

    @Test
    public void sizeStringToApproxWidthBlindPrefersClosestCharacter() {
        String text = "abcdefghijkl";
        int targetWidth = LineFormattingUtil.getMetrics().stringWidth(text) - 3;
        int approx = LineFormattingUtil.sizeStringToApproxWidthBlind(text, targetWidth);
        assertThat(approx >= text.length() - 1, is(true));
    }
}
