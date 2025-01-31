package kamkeel.plugeditor.book;

import java.util.ArrayList;
import java.util.List;

public class Page {
    public List<Line> lines = new ArrayList<Line>();

    public Page() {
        this.lines.add(new Line());
    }

    /**
     * Attempts to add `strAdd` to the specified line/character position.
     * If the resulting text or line count is over capacity, it might carry
     * the overflow to the next line, or even to the returned "overflow" string
     * for the calling code to handle.
     */
    public String addText(int lineNum, int charPos, String strAdd) {
        // If we already have 13 lines, we won't insert more text here;
        // we just return the leftover so the caller (Book.addText) can
        // handle it or create a new Page.
        if (lineNum >= 13)
            return strAdd;

        // Clamp lineNum
        if (lineNum < 0) {
            lineNum = 0;
        } else if (lineNum > this.lines.size()) {
            lineNum = this.lines.size();
        }

        // If lineNum == lines.size(), create a new line
        if (lineNum == this.lines.size()) {
            this.lines.add(new Line());
        }

        // Grab current line
        Line currLine = this.lines.get(lineNum);

        // Inherit formatting from the previous line if we are not on the first
        if (lineNum > 0) {
            currLine.wrappedFormatting = this.lines.get(lineNum - 1).getActiveFormatting();
        } else {
            currLine.wrappedFormatting = "";
        }

        // Actually add the text to the line, potentially wrapping within the line
        String lineOverflow = currLine.addText(charPos, strAdd);

        // Check for the total char limit on this page (255)
        // This is a vanilla-like limit for a single page's text.
        int prevLinesCharCount = 0;
        for (int i = 0; i < lineNum; i++) {
            prevLinesCharCount += this.lines.get(i).text.length();
        }

        if (prevLinesCharCount + currLine.text.length() > 255) {
            // The current line has exceeded the 255-char limit for this page.
            // We split off the extra into `pageOverflow`.
            String pageOverflow = "";

            // Remove any lines after `lineNum` and prepend their text to `pageOverflow`
            for (int j = this.lines.size() - 1; j > lineNum; j--) {
                pageOverflow = this.lines.get(j).text + pageOverflow;
                this.lines.remove(j);
            }

            // Determine where to split off the current line
            int splitAt = 255 - prevLinesCharCount;
            if (splitAt < 0) splitAt = 0;

            // If the char at splitAt isn't a space, try to split at the last space
            if (currLine.text.charAt(splitAt) != ' ') {
                int spacePos = currLine.text.lastIndexOf(' ', splitAt);
                if (spacePos == -1) {
                    splitAt = 0;
                } else {
                    splitAt = spacePos;
                }
            }

            // If we can't split, everything goes into overflow
            if (splitAt == 0) {
                pageOverflow = currLine.text + lineOverflow + pageOverflow;
                this.lines.remove(lineNum);
            } else {
                // Keep up to `splitAt` in this line, put remainder into pageOverflow
                pageOverflow = currLine.text.substring(splitAt) + lineOverflow + pageOverflow;
                currLine.text = currLine.text.substring(0, splitAt);
            }
            return pageOverflow;
        }

        // If there's leftover text from the line's wrapping,
        // recursively push it to the next line in this Page (or return leftover).
        if (!lineOverflow.isEmpty()) {
            return addText(lineNum + 1, 0, lineOverflow);
        }

        return "";
    }

    public void clear() {
        this.lines.clear();
    }

    /**
     * "Pad" the Page to ensure it has 13 lines total, but DO NOT append
     * forced `"\n"` to each line. This was the main cause of accidental
     * extra blank lines building up.
     */
    public static Page pad(Page page) {
        // =========================
        // FIX #1: Remove forced newline from the last line
        // =========================
        // Old code was:
        //    if (Line.getStringWidth(lastLine.wrappedFormatting + lastLine.text) < 116)
        //        lastLine.text += "\n";
        //
        // which artificially added a "\n" if the final line was under 116 px wide.

        // We'll still ensure at least 1 line
        if (page.lines.isEmpty()) {
            page.lines.add(new Line());
        }

        // Only add enough lines so we have 13 total.
        // But DO NOT fill them with `"\n"`.
        Line lastLine = page.lines.get(page.lines.size() - 1);
        while (page.lines.size() < 13) {
            Line newLine = new Line();
            // =========================
            // FIX #2: Do not force `newLine.text = "\n"`.
            // Instead, keep it empty.
            // =========================
            newLine.text = "";
            page.lines.add(newLine);
        }
        return page;
    }

    public String asString() {
        String out = "";
        for (Line line : this.lines) {
            out += line.text;
        }
        return out;
    }

    public int charCount() {
        int total = 0;
        for (Line line : this.lines) {
            total += line.text.length();
        }
        return total;
    }

    public void dump() {
        System.out.println("##############################################################################");
        for (Line line : this.lines) {
            System.out.println("WF:|" + line.wrappedFormatting + "|TX:|" + line.text.replaceAll("\n", "\\\\n") + "|");
        }
        System.out.println("##############################################################################\n\n");
    }

    public boolean isEmpty() {
        for (Line line : this.lines) {
            if (!line.text.isEmpty())
                return false;
        }
        return true;
    }
}
