package kamkeel.bookeditor.util;

import kamkeel.bookeditor.format.FormatterTestScenario;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public class FormattingUtilTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        return FormatterTestScenario.scenarios();
    }

    @Parameterized.Parameter(0)
    public String name;

    @Parameterized.Parameter(1)
    public FormatterTestScenario scenario;

    @Before
    public void setUp() {
        scenario.apply();
    }

    @After
    public void tearDown() {
        scenario.reset();
    }

    @Test
    public void detectFormattingCodeLengthHandlesValidCodes() {
        assertThat(FormattingUtil.detectFormattingCodeLength("§aHello", 0), is(2));
        assertThat(FormattingUtil.detectFormattingCodeLength("NoCode", 0), is(0));
        if (scenario.isHexText()) {
            assertThat(FormattingUtil.detectFormattingCodeLength("§#AABBCC", 0), is(8));
        } else {
            assertThat(FormattingUtil.detectFormattingCodeLength("§#AABBCC", 0), is(1));
        }
    }

    @Test
    public void detectAmpersandCodesRespectsConfiguration() {
        assumeTrue(scenario.isHexText());
        int detected = FormattingUtil.detectFormattingCodeLength("&lBold", 0);
        if (scenario.isAmpersandEnabled()) {
            assertThat(detected, is(2));
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
    public void findFormattingCodeStartSupportsHexSequences() {
        assumeTrue(scenario.isHexText());
        String text = "§#FFAACCColor";
        int end = text.indexOf('C', 8);
        assertThat(FormattingUtil.findFormattingCodeStart(text, end), is(0));
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
        if (scenario.isHexText()) {
            String hexFormatted = "§#FFAACCColourful";
            assertThat(FormattingUtil.sanitizeFormatting(hexFormatted), is(hexFormatted));
        }
    }
}
