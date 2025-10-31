package kamkeel.bookeditor.util;

import kamkeel.bookeditor.BookController;
import kamkeel.bookeditor.book.Line;
import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.FormatterTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class LineFormattingUtilTest {
    private final Supplier<BookFormatter> formatterSupplier;

    public LineFormattingUtilTest(String name, Supplier<BookFormatter> formatterSupplier) {
        this.formatterSupplier = formatterSupplier;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return FormatterTestHelper.allFormatterSuppliers();
    }

    @Before
    public void setUpMetrics() {
        BookController.setFormatterForTesting(formatterSupplier.get());
        LineFormattingUtil.setMetrics(new SimpleTextMetrics());
    }

    @After
    public void tearDownFormatter() {
        BookController.resetFormatterForTesting();
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
    public void sizeStringToApproxWidthBlindPrefersClosestCharacter() {
        String text = "abcdefghijkl";
        int targetWidth = LineFormattingUtil.getMetrics().stringWidth(text) - 3;
        int approx = LineFormattingUtil.sizeStringToApproxWidthBlind(text, targetWidth);
        assertThat(approx >= text.length() - 1, is(true));
    }
}
