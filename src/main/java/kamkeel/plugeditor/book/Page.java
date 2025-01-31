package kamkeel.plugeditor.book;

import java.util.ArrayList;
import java.util.List;

public class Page {
    public List<Line> lines = new ArrayList<Line>();

    public Page() {
        this.lines.add(new Line());
    }

    public String addText(int lineNum, int charPos, String strAdd) {
        if (lineNum >= 13)
            return strAdd;
        if (lineNum < 0) {
            lineNum = 0;
        } else if (lineNum > this.lines.size()) {
            lineNum = this.lines.size();
        }
        if (lineNum == this.lines.size())
            this.lines.add(new Line());
        Line currLine = this.lines.get(lineNum);
        if (lineNum > 0) {
            currLine.wrappedFormatting = ((Line) this.lines.get(lineNum - 1)).getActiveFormatting();
        } else {
            currLine.wrappedFormatting = "";
        }
        String lineOverflow = currLine.addText(charPos, strAdd);
        int prevLinesCharCount = 0;
        for (int i = 0; i < lineNum; i++)
            prevLinesCharCount += ((Line) this.lines.get(i)).text.length();
        if (prevLinesCharCount + currLine.text.length() > 255) {
            String pageOverflow = "";
            for (int j = this.lines.size() - 1; j > lineNum; j--) {
                pageOverflow = ((Line) this.lines.get(j)).text + pageOverflow;
                this.lines.remove(j);
            }
            int splitAt = 255 - prevLinesCharCount;
            if (splitAt < 0)
                splitAt = 0;
            if (currLine.text.charAt(splitAt) != ' ') {
                int spacePos = currLine.text.lastIndexOf(' ', splitAt);
                if (spacePos == -1) {
                    splitAt = 0;
                } else {
                    splitAt = spacePos;
                }
            }
            if (splitAt == 0) {
                pageOverflow = currLine.text + lineOverflow + pageOverflow;
                this.lines.remove(lineNum);
            } else {
                pageOverflow = currLine.text.substring(splitAt) + lineOverflow + pageOverflow;
                currLine.text = currLine.text.substring(0, splitAt);
            }
            return pageOverflow;
        }
        if (!lineOverflow.isEmpty())
            return addText(lineNum + 1, 0, lineOverflow);
        return "";
    }

    public void clear() {
        this.lines.clear();
    }

    public static Page pad(Page page) {
        Line lastLine = page.lines.get(page.lines.size() - 1);
        if (Line.getStringWidth(lastLine.wrappedFormatting + lastLine.text) < 116)
            lastLine.text += "\n";
        while (page.lines.size() < 13) {
            Line newLine = new Line();
            newLine.text = "\n";
            page.lines.add(newLine);
        }
        return page;
    }

    public String asString() {
        String out = "";
        for (Line line : this.lines)
            out = out + line.text;
        return out;
    }

    public int charCount() {
        int total = 0;
        for (Line line : this.lines)
            total += line.text.length();
        return total;
    }

    public void dump() {
        System.out.println("##############################################################################");
        for (Line line : this.lines)
            System.out.println("WF:|" + line.wrappedFormatting + "|TX:|" + line.text.replaceAll("\n", "\\\\n") + "|");
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
