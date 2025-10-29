package kamkeel.bookeditor.book;

import kamkeel.bookeditor.format.HexTextFormatter;
import kamkeel.bookeditor.format.StandaloneFormatter;
import kamkeel.bookeditor.util.FormattingUtil;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BookControllerTest {

    @After
    public void reset() {
        BookController.resetFormatter();
    }

    @Test
    public void defaultFormatterIsStandalone() {
        BookController.resetFormatter();
        assertThat(BookController.getFormatter(), instanceOf(StandaloneFormatter.class));
    }

    @Test
    public void canSwitchToHexFormatter() {
        BookController.setFormatter(new HexTextFormatter());
        assertThat(BookController.getFormatter(), instanceOf(HexTextFormatter.class));
        assertThat(FormattingUtil.detectFormattingCodeLength("&aabbcc", 0), is(7));
    }
}
