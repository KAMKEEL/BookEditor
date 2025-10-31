package kamkeel.bookeditor.util;

import kamkeel.bookeditor.BookController;
import kamkeel.bookeditor.format.BookFormatter;
import kamkeel.bookeditor.format.HexTextBookFormatter;
import kamkeel.bookeditor.format.StandaloneBookFormatter;
import org.junit.After;
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
        return Arrays.asList(new Object[][]{
            {"Standalone", new StandaloneBookFormatter(), false, false},
            {"HexText (ampersand off)", new HexTextBookFormatter(() -> false, () -> true), true, false},
            {"HexText (ampersand on)", new HexTextBookFormatter(() -> true, () -> true), true, true}
        });
    }

    private final BookFormatter formatter;
    private final boolean supportsHex;
    private final boolean ampersandEnabled;

    public FormattingUtilTest(String name, BookFormatter formatter, boolean supportsHex, boolean ampersandEnabled) {
        this.formatter = formatter;
        this.supportsHex = supportsHex;
        this.ampersandEnabled = ampersandEnabled;
    }

    @Before
    public void setUpFormatter() {
        BookController.setFormatter(formatter);
    }

    @After
    public void tearDownFormatter() {
        BookController.setFormatter(new StandaloneBookFormatter());
    }

    @Test
    public void detectFormattingCodeLengthHandlesValidCodes() {
        assertThat(FormattingUtil.detectFormattingCodeLength("§aHello", 0), is(2));
        assertThat(FormattingUtil.detectFormattingCodeLength("NoCode", 0), is(0));
        assertThat(FormattingUtil.detectFormattingCodeLength("§", 0), is(1));
    }

    @Test
    public void detectFormattingUnderstandsHexCodesWhenAvailable() {
        int detected = FormattingUtil.detectFormattingCodeLength("§#AABBCC", 0);
        assertThat(detected, is(supportsHex ? 8 : 1));
    }

    @Test
    public void detectFormattingHonoursAmpersandToggle() {
        int detected = FormattingUtil.detectFormattingCodeLength("&aHello", 0);
        if (formatter instanceof HexTextBookFormatter) {
            assertThat(detected, is(ampersandEnabled ? 2 : 0));
        } else {
            assertThat(detected, is(0));
        }
    }

    @Test
    public void findFormattingCodeStartFindsPreviousMarker() {
        String text = "§aHello";
        int end = text.indexOf('H');
        assertThat(FormattingUtil.findFormattingCodeStart(text, end), is(0));
        assertThat(FormattingUtil.findFormattingCodeStart("§l§oBold", 4), is(2));
    }

    @Test
    public void sanitizeFormattingDropsDanglingMarkers() {
        String sanitized = FormattingUtil.sanitizeFormatting("Line§");
        assertThat(sanitized, is("Line"));
    }

    @Test
    public void sanitizeFormattingKeepsValidSequences() {
        String formatted = "§aGreen §lBold";
        assertThat(FormattingUtil.sanitizeFormatting(formatted), is(formatted));
    }

    @Test
    public void stripColorCodesRemovesFormattingMarkers() {
        String formatted = supportsHex ? "§#abcdefBold" : "§aBold";
        assertThat(FormattingUtil.stripColorCodes(formatted), is("Bold"));
        if (supportsHex) {
            assertThat(FormattingUtil.stripColorCodes("<ABCDEF>RGB</ABCDEF>"), is("RGB"));
        }
    }
}
