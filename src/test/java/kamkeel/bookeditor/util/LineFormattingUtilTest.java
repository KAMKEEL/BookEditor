package kamkeel.bookeditor.util;

import kamkeel.bookeditor.book.BookController;
import kamkeel.bookeditor.book.Line;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LineFormattingUtilTest {

    @Before
    public void setUpMetrics() {
        BookController.overrideFormatterForTesting(BookController.getStandaloneFormatter());
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
    public void wrapStringToWidthPrefersPreviousWhitespaceWhenOverLimit() {
        String wrapped = LineFormattingUtil.wrapStringToWidth("Word1 Word2 Word3", 60, "");
        String[] segments = wrapped.split(String.valueOf(Line.SPLIT_CHAR));
        assertThat("First segment should not cut Word2", segments[0], is("Word1 "));
        assertThat("Second segment should start with Word2", segments[1].startsWith("Word2"), is(true));
    }

    @Test
    public void listFormattedStringToWidthMaintainsFormatting() {
        List<String> segments = LineFormattingUtil.listFormattedStringToWidth("§aColoured text continues", "");
        assertThat(segments.get(0).startsWith("§a"), is(true));
        assertThat(LineFormattingUtil.getActiveFormatting(segments.get(0)), is("§a"));
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
