package kamkeel.bookeditor.util;

import kamkeel.bookeditor.book.BookController;
import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.HexTextBookFormatter;
import kamkeel.bookeditor.format.StandaloneBookFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class FormattingUtilTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"Standalone", new StandaloneBookFormatter(), 1},
            {"HexText", new HexTextBookFormatter(), 0}
        });
    }

    private final BookFormatter formatter;
    private final int expectedDanglingLength;

    public FormattingUtilTest(String name, BookFormatter formatter, int expectedDanglingLength) {
        this.formatter = formatter;
        this.expectedDanglingLength = expectedDanglingLength;
    }

    @Before
    public void resetFormatter() {
        BookController.overrideFormatterForTesting(formatter);
    }

    @Test
    public void detectFormattingCodeLengthHandlesValidCodes() {
        assertThat(FormattingUtil.detectFormattingCodeLength("§aHello", 0), is(2));
        assertThat(FormattingUtil.detectFormattingCodeLength("NoCode", 0), is(0));
        assertThat(FormattingUtil.detectFormattingCodeLength("§", 0), is(expectedDanglingLength));
    }

    @Test
    public void findFormattingCodeStartFindsPreviousMarker() {
        String text = "§aHello";
        int end = text.indexOf('H');
        assertThat(FormattingUtil.findFormattingCodeStart(text, end), is(0));
        assertThat(FormattingUtil.findFormattingCodeStart("§l§oBold", 4), is(2));
    }

    @Test
    public void sanitizeFormattingDropsDanglingSection() {
        String sanitized = FormattingUtil.sanitizeFormatting("Line§");
        assertThat(sanitized, is("Line"));
    }

    @Test
    public void sanitizeFormattingKeepsValidSequences() {
        String formatted = "§aGreen §lBold";
        assertThat(FormattingUtil.sanitizeFormatting(formatted), is(formatted));
    }

    @Test
    public void stripColorCodesRemovesLegacyCodes() {
        assertThat(FormattingUtil.stripColorCodes("§aGreen"), is("Green"));
    }
}
