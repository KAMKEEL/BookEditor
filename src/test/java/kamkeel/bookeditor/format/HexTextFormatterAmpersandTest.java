package kamkeel.bookeditor.format;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HexTextFormatterAmpersandTest {

    @Test
    public void ampersandDisabledTreatsMarkerAsLiteral() {
        HexTextFormatter formatter = new HexTextFormatter(() -> false, () -> false);
        assertThat(formatter.detectFormattingCodeLength("&aBold", 0), is(0));
        assertThat(formatter.stripColorCodes("&aBold"), is("&aBold"));
    }

    @Test
    public void ampersandEnabledRecognisesFormatting() {
        HexTextFormatter formatter = new HexTextFormatter(() -> true, () -> false);
        assertThat(formatter.detectFormattingCodeLength("&aBold", 0) > 0, is(true));
        assertThat(formatter.stripColorCodes("&aBold"), is("Bold"));
    }
}
