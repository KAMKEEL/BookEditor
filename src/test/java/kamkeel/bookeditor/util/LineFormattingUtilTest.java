package kamkeel.bookeditor.util;

import kamkeel.bookeditor.BookController;
import kamkeel.bookeditor.book.Line;
import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.HexTextBookFormatter;
import kamkeel.bookeditor.format.StandaloneBookFormatter;
import org.junit.After;
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
        return Arrays.asList(new Object[][]{
            {"Standalone", new StandaloneBookFormatter(), false},
            {"HexText", new HexTextBookFormatter(() -> true, () -> true), true}
        });
    }

    private final BookFormatter formatter;
    private final boolean supportsHex;

    public LineFormattingUtilTest(String name, BookFormatter formatter, boolean supportsHex) {
        this.formatter = formatter;
        this.supportsHex = supportsHex;
    }

    @Before
    public void setUp() {
        LineFormattingUtil.setMetrics(new SimpleTextMetrics());
        BookController.setFormatter(formatter);
    }

    @After
    public void tearDown() {
        BookController.setFormatter(new StandaloneBookFormatter());
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
    public void getActiveFormattingAccumulatesStyles() {
        String formatted = "§aGreen §lBold";
        assertThat(LineFormattingUtil.getActiveFormatting(formatted), is("§a§l"));
    }

    @Test
    public void getActiveFormattingUnderstandsHexAndHtmlCodes() {
        if (!supportsHex) {
            assertThat(LineFormattingUtil.getActiveFormatting("§#AABBCC"), is(""));
            return;
        }
        assertThat(LineFormattingUtil.getActiveFormatting("§#AABBCC"), is("§#aabbcc"));
        assertThat(LineFormattingUtil.getActiveFormatting("<ABCDEF>Test"), is("<ABCDEF>"));
    }

    @Test
    public void getActiveFormattingRecognisesAmpersandWhenEnabled() {
        String formatting = LineFormattingUtil.getActiveFormatting("&aBold");
        if (supportsHex) {
            assertThat(formatting, is("§a"));
        } else {
            assertThat(formatting, is(""));
        }
    }

    @Test
    public void sizeStringToApproxWidthBlindPrefersClosestCharacter() {
        String text = "abcdefghijkl";
        int targetWidth = LineFormattingUtil.getMetrics().stringWidth(text) - 3;
        int approx = LineFormattingUtil.sizeStringToApproxWidthBlind(text, targetWidth);
        assertThat(approx >= text.length() - 1, is(true));
    }
}
