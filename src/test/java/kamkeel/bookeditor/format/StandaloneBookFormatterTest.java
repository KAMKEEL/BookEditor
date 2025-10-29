package kamkeel.bookeditor.format;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StandaloneBookFormatterTest {
    private StandaloneBookFormatter formatter;

    @Before
    public void setUp() {
        formatter = new StandaloneBookFormatter();
    }

    @Test
    public void detectFormattingCodeLengthHandlesValidCodes() {
        assertThat(formatter.detectFormattingCodeLength("§aHello", 0), is(2));
        assertThat(formatter.detectFormattingCodeLength("NoCode", 0), is(0));
        assertThat(formatter.detectFormattingCodeLength("§", 0), is(1));
    }

    @Test
    public void findFormattingCodeStartFindsPreviousMarker() {
        String text = "§aHello";
        int end = text.indexOf('H');
        assertThat(formatter.findFormattingCodeStart(text, end), is(0));
        assertThat(formatter.findFormattingCodeStart("§l§oBold", 4), is(2));
    }

    @Test
    public void sanitizeFormattingDropsDanglingSection() {
        String sanitized = formatter.sanitizeFormatting("Line§");
        assertThat(sanitized, is("Line"));
    }

    @Test
    public void sanitizeFormattingKeepsValidSequences() {
        String formatted = "§aGreen §lBold";
        assertThat(formatter.sanitizeFormatting(formatted), is(formatted));
    }

    @Test
    public void stripColorCodesRemovesFormatting() {
        String formatted = "§aGreen §lBold";
        assertThat(formatter.stripColorCodes(formatted), is("Green Bold"));
    }
}
