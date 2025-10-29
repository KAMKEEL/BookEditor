package kamkeel.bookeditor.util;

import kamkeel.bookeditor.book.BookController;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FormattingUtilTest {

    @Before
    public void resetFormatter() {
        BookController.overrideFormatterForTesting(BookController.getStandaloneFormatter());
    }

    @Test
    public void detectFormattingCodeLengthHandlesValidCodes() {
        assertThat(FormattingUtil.detectFormattingCodeLength("§aHello", 0), is(2));
        assertThat(FormattingUtil.detectFormattingCodeLength("NoCode", 0), is(0));
        assertThat(FormattingUtil.detectFormattingCodeLength("§", 0), is(1));
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
