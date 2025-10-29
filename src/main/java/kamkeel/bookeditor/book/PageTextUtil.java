package kamkeel.bookeditor.book;

/**
 * Helper routines for working with {@link Page} instances. Centralising these
 * behaviours avoids duplicating logic across different parts of the editor.
 */
public final class PageTextUtil {
    private PageTextUtil() {
    }

    public static Page pad(Page page) {
        Line lastLine = page.lines.get(page.lines.size() - 1);
        if (Line.getStringWidth(lastLine.wrappedFormatting + lastLine.text) < Line.BOOK_TEXT_WIDTH) {
            lastLine.text += "\n";
        }
        while (page.lines.size() < 13) {
            Line newLine = new Line();
            newLine.text = "\n";
            page.lines.add(newLine);
        }
        return page;
    }

    public static int charCount(Page page) {
        int total = 0;
        for (Line line : page.lines) {
            total += line.text.length();
        }
        return total;
    }

    public static boolean isEmpty(Page page) {
        for (Line line : page.lines) {
            if (!line.text.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
