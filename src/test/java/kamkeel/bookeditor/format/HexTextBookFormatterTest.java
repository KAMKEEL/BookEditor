package kamkeel.bookeditor.format;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HexTextBookFormatterTest {
    private HexTextBookFormatter formatter;

    @Before
    public void setUp() {
        formatter = new HexTextBookFormatter();
    }

    @Test
    public void detectFormattingCodeLengthHandlesAmpersandHex() {
        assertThat(formatter.detectFormattingCodeLength("&123456", 0), is(7));
    }

    @Test
    public void detectFormattingCodeLengthHandlesAngleBracketHex() {
        assertThat(formatter.detectFormattingCodeLength("<abcdef>", 0), is(8));
        assertThat(formatter.detectFormattingCodeLength("</abcdef>", 0), is(9));
    }

    @Test
    public void findFormattingCodeStartDetectsHexSequences() {
        String sample = "&123456Text";
        assertThat(formatter.findFormattingCodeStart(sample, 7), is(0));

        String angled = "<abcdef>Color";
        assertThat(formatter.findFormattingCodeStart(angled, 8), is(0));
    }

    @Test
    public void stripColorCodesRemovesHexMarkers() {
        String stripped = formatter.stripColorCodes("Hello&123456World");
        assertThat(stripped, is("HelloWorld"));
    }

    @Test
    public void getActiveFormattingReturnsLatestCodes() {
        String formatting = formatter.getActiveFormatting("<abcdef>§lBright");
        assertThat(formatting, is("<abcdef>§l"));
    }

    @Test
    public void sanitizeFormattingKeepsCompleteHexCodes() {
        String value = "<abcdef>RGB";
        assertThat(formatter.sanitizeFormatting(value), is(value));
    }
}
