package kamkeel.bookeditor.format;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HexTextFormatterTest {
    private final HexTextFormatter formatter = new HexTextFormatter();

    @Test
    public void detectFormattingCodeLengthSupportsHexSequences() {
        assertThat(formatter.detectFormattingCodeLength("&abcdef", 0), is(7));
        assertThat(formatter.detectFormattingCodeLength("<abcdef>", 0), is(8));
        assertThat(formatter.detectFormattingCodeLength("</abcdef>", 0), is(9));
        assertThat(formatter.detectFormattingCodeLength("§aLegacy", 0), is(2));
    }

    @Test
    public void stripColorCodesRemovesExtendedSequences() {
        String formatted = "<abcdef>Test&lLine&123456";
        assertThat(formatter.stripColorCodes(formatted), is("TestLine"));
    }

    @Test
    public void getActiveFormattingIncludesHexCodes() {
        String formatted = "<abcdef>Test&lLine";
        assertThat(formatter.getActiveFormatting(formatted), is("<abcdef>&l"));
        assertThat(formatter.getFormatFromString(formatted), is("<abcdef>&l"));
    }
}
