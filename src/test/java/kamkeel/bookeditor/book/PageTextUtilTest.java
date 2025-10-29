package kamkeel.bookeditor.book;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PageTextUtilTest {

    @Test
    public void padAddsTrailingLines() {
        Page page = new Page();
        page.lines.clear();
        Line line = new Line();
        line.text = "Content";
        page.lines.add(line);

        PageTextUtil.pad(page);

        assertThat(page.lines.size(), is(13));
    }

    @Test
    public void charCountSumsAllLines() {
        Page page = new Page();
        page.lines.clear();
        Line first = new Line();
        first.text = "One";
        Line second = new Line();
        second.text = "Two";
        page.lines.add(first);
        page.lines.add(second);

        assertThat(PageTextUtil.charCount(page), is(6));
    }

    @Test
    public void isEmptyDetectsContent() {
        Page page = new Page();
        page.lines.clear();
        Line line = new Line();
        line.text = "";
        page.lines.add(line);

        assertThat(PageTextUtil.isEmpty(page), is(true));
        line.text = "text";
        assertThat(PageTextUtil.isEmpty(page), is(false));
    }
}
