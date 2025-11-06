package kamkeel.bookeditor.book;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import kamkeel.bookeditor.book.format.FormattingOptions;
import kamkeel.bookeditor.book.format.FormattingUtil;
import org.junit.Test;

public class FormattingUtilTest {

    @Test
    public void detectSectionHexCodeLength() {
        FormattingOptions options = FormattingOptions.of(false, false, true);
        String text = "\u00a7#12AB34rest";
        int len = FormattingUtil.detectFormattingCodeLength(text, 0, options);
        assertThat(len, is(8));
    }

    @Test
    public void detectAmpersandFormattingWhenEnabled() {
        FormattingOptions options = FormattingOptions.of(true, false, true);
        String text = "&aHello";
        int len = FormattingUtil.detectFormattingCodeLength(text, 0, options);
        assertThat(len, is(2));
    }

    @Test
    public void ignoreAmpersandFormattingWhenDisabled() {
        FormattingOptions options = FormattingOptions.of(false, false, true);
        String text = "&aHello";
        int len = FormattingUtil.detectFormattingCodeLength(text, 0, options);
        assertThat(len, is(0));
        String active = FormattingUtil.collectActiveFormatting(text, options);
        assertThat(active, equalTo(""));
    }

    @Test
    public void htmlFormattingProducesEquivalentActiveFormatting() {
        FormattingOptions options = FormattingOptions.of(false, true, true);
        String html = "<#123456>Line";
        String formatting = FormattingUtil.collectActiveFormatting(html, options);
        assertThat(formatting, equalTo("\u00a7#123456"));
    }

    @Test
    public void sanitizeDropsTruncatedCodes() {
        FormattingOptions options = FormattingOptions.of(true, true, true);
        String sanitized = FormattingUtil.sanitizeFormatting("Hello\u00a7#123", options);
        assertThat(sanitized, equalTo("Hello"));
    }
}
