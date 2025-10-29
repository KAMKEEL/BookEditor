package kamkeel.bookeditor.format;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HexTextFormatterTest {
    private HexTextFormatter formatter;

    @Before
    public void setUp() {
        formatter = new HexTextFormatter();
    }

    @Test
    public void detectFormattingCodeLengthHandlesHexColor() {
        assertThat(formatter.detectFormattingCodeLength("&FFAA00Hello", 0), is(7));
    }

    @Test
    public void detectFormattingCodeLengthHandlesGradientMarkers() {
        assertThat(formatter.detectFormattingCodeLength("<FFAA00>Hello", 0), is(8));
    }

    @Test
    public void findFormattingCodeStartFindsAmpersandCode() {
        String text = "&FFAA00Hello";
        assertThat(formatter.findFormattingCodeStart(text, 7), is(0));
    }

    @Test
    public void stripColorCodesRemovesHexSequences() {
        assertThat(formatter.stripColorCodes("&FFAA00Hello"), is("Hello"));
    }

    @Test
    public void getActiveFormattingReturnsHexPrefix() {
        assertThat(formatter.getActiveFormatting("&FFAA00Hello"), is("&FFAA00"));
    }

    @Test
    public void isFormatColorTreatsGradientCodeAsColor() {
        assertThat(formatter.isFormatColor('g'), is(true));
    }
}
