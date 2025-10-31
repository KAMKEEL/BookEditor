package kamkeel.bookeditor.util;

import kamkeel.bookeditor.book.Line;
import kamkeel.bookeditor.format.FormatterTestScenario;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public class LineFormattingUtilTest {

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
    public void listFormattedStringToWidthSupportsAmpersandWhenEnabled() {
        assumeTrue(scenario.isHexText() && scenario.isAmpersandEnabled());
        List<String> segments = LineFormattingUtil.listFormattedStringToWidth("&aColoured text", "");
        assertThat(segments.get(0).startsWith("&a"), is(true));
        assertThat(LineFormattingUtil.getActiveFormatting(segments.get(0)), is("&a"));
    }

    @Test
    public void getActiveFormattingAccumulatesStyles() {
        String formatted = "§aGreen §lBold";
        assertThat(LineFormattingUtil.getActiveFormatting(formatted), is("§a§l"));
        if (scenario.isHexText()) {
            String hexFormatted = "§#FFAACCColour";
            assertThat(LineFormattingUtil.getActiveFormatting(hexFormatted), is("§#FFAACC"));
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
