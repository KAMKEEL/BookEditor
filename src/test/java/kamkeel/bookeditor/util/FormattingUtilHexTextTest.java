package kamkeel.bookeditor.util;

import kamkeel.bookeditor.book.BookController;
import kamkeel.bookeditor.format.HexTextBookFormatter;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FormattingUtilHexTextTest {

    @Before
    public void activateHexTextFormatter() {
        BookController.overrideFormatterForTesting(new HexTextBookFormatter());
    }

    @Test
    public void detectFormattingCodeLengthRecognisesAmpersandStyles() {
        assertThat(FormattingUtil.detectFormattingCodeLength("&lBold", 0), is(2));
        assertThat(FormattingUtil.detectFormattingCodeLength("&oItalic", 0), is(2));
    }

    @Test
    public void findFormattingCodeStartHandlesAmpersandStyles() {
        String text = "&lBold";
        int end = text.indexOf('B');
        assertThat(FormattingUtil.findFormattingCodeStart(text, end), is(0));
    }

    @Test
    public void sanitizeFormattingRemovesDanglingAmpersand() {
        assertThat(FormattingUtil.sanitizeFormatting("Text&"), is("Text"));
    }

    @Test
    public void stripColorCodesRemovesAmpersandStyles() {
        assertThat(FormattingUtil.stripColorCodes("&lBold"), is("Bold"));
    }
}
