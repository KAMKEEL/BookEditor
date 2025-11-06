package kamkeel.bookeditor.book;

import java.util.ArrayList;
import java.util.List;

import kamkeel.bookeditor.book.format.FormattingOptions;
import kamkeel.bookeditor.book.text.PageEditor;

/**
 * Represents a single page in a Book. A Page is composed of multiple Lines and
 * provides helpers for adding text, checking emptiness and formatting padding
 * so that page data fits within Minecraft's constraints.
 */
public class Page {
    public List<Line> lines = new ArrayList<Line>();

    public Page() {
        this.lines.add(new Line());
    }

    public String addText(int lineNum, int charPos, String strAdd) {
        return addText(lineNum, charPos, strAdd, FormattingOptions.defaults());
    }

    public String addText(int lineNum, int charPos, String strAdd, FormattingOptions options) {
        return PageEditor.insert(this, lineNum, charPos, strAdd, options);
    }

    public void clear() {
        this.lines.clear();
    }

    public static Page pad(Page page) {
        return PageEditor.pad(page);
    }

    public String asString() {
        StringBuilder out = new StringBuilder();
        for (Line line : this.lines) {
            out.append(line.text);
        }
        return out.toString();
    }

    public int charCount() {
        return PageEditor.totalCharacters(this);
    }

    public void dump() {
        System.out.println("##############################################################################");
        for (Line line : this.lines) {
            System.out.println("WF:|" + line.wrappedFormatting + "|TX:|" + line.text.replaceAll("\n", "\\\\n") + "|");
        }
        System.out.println("##############################################################################\n\n");
    }

    public boolean isEmpty() {
        return PageEditor.isEmpty(this);
    }
}
