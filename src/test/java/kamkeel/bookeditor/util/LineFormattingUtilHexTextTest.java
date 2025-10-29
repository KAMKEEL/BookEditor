package kamkeel.bookeditor.util;

import kamkeel.bookeditor.book.BookController;
import kamkeel.bookeditor.book.Line;
import kamkeel.bookeditor.format.HexTextBookFormatter;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LineFormattingUtilHexTextTest {

    @Before
    public void activateHexTextFormatter() {
        BookController.overrideFormatterForTesting(new HexTextBookFormatter());
        LineFormattingUtil.setMetrics(new SimpleTextMetrics());
    }

    @Test
    public void getActiveFormattingCapturesAmpersandStyles() {
        String formatted = "&lBold text";
        assertThat(LineFormattingUtil.getActiveFormatting(formatted), is("&l"));
    }

    @Test
    public void wrapStringToWidthCarriesAmpersandFormatting() {
        String wrapped = LineFormattingUtil.wrapStringToWidth("&lBold text wraps", 40, "");
        String[] segments = wrapped.split(String.valueOf(Line.SPLIT_CHAR));
        assertThat(segments[0].startsWith("&l"), is(true));
        assertThat(LineFormattingUtil.getActiveFormatting(segments[0]), is("&l"));
    }
}
