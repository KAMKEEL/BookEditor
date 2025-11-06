package kamkeel.bookeditor.book;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.junit.Test;

public class PageTest extends AbstractBookTest {

    @Test
    public void addTextWrapsAcrossMultipleLines() {
        Page page = new Page();
        Line first = page.lines.get(0);
        String longText = repeat('x', 150);

        String overflow = page.addText(0, 0, longText);

                assertThat("Original line trimmed to width", first.text.length(), is(19));
        assertThat("New lines were created for remainder", page.lines.size() > 1, is(true));
        assertThat(page.lines.get(1).text.length() > 0, is(true));
    }

    @Test
    public void padFillsPageToThirteenLinesWithTrailingNewlines() {
        Page page = new Page();
        page.lines.get(0).text = "Hello";

        Page padded = Page.pad(page);
        List<Line> lines = padded.lines;

        assertThat(lines, hasSize(13));
        assertThat(lines.get(0).text, equalTo("Hello\n"));
        for (int i = 1; i < lines.size(); i++) {
            assertThat("Padding lines should be single newline", lines.get(i).text, equalTo("\n"));
        }
    }

    private static String repeat(char c, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(c);
        }
        return builder.toString();
    }
}
