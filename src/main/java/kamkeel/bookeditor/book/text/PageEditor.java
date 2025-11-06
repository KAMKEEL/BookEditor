package kamkeel.bookeditor.book.text;

import kamkeel.bookeditor.book.Line;
import kamkeel.bookeditor.book.Page;
import kamkeel.bookeditor.book.format.FormattingOptions;

public final class PageEditor {
    private PageEditor() {
    }

    public static String insert(Page page, int lineNum, int charPos, String addition, FormattingOptions options) {
        if (lineNum >= 13) {
            return addition;
        }
        if (lineNum < 0) {
            lineNum = 0;
        } else if (lineNum > page.lines.size()) {
            lineNum = page.lines.size();
        }
        if (lineNum == page.lines.size()) {
            page.lines.add(new Line());
        }
        Line currentLine = page.lines.get(lineNum);
        if (lineNum > 0) {
            Line previous = page.lines.get(lineNum - 1);
            currentLine.wrappedFormatting = LineEditor.collectActiveFormatting(previous.getTextWithWrappedFormatting(), options);
        } else {
            currentLine.wrappedFormatting = "";
        }
        String overflow = LineEditor.insert(currentLine, charPos, addition, options);
        int prevLinesCharCount = 0;
        for (int i = 0; i < lineNum; i++) {
            prevLinesCharCount += page.lines.get(i).text.length();
        }
        if (prevLinesCharCount + currentLine.text.length() > 255) {
            StringBuilder pageOverflow = new StringBuilder();
            for (int j = page.lines.size() - 1; j > lineNum; j--) {
                pageOverflow.insert(0, page.lines.get(j).text);
                page.lines.remove(j);
            }
            int splitAt = 255 - prevLinesCharCount;
            if (splitAt < 0) {
                splitAt = 0;
            }
            if (currentLine.text.charAt(splitAt) != ' ') {
                int spacePos = currentLine.text.lastIndexOf(' ', splitAt);
                splitAt = spacePos == -1 ? 0 : spacePos;
            }
            if (splitAt == 0) {
                pageOverflow.insert(0, currentLine.text + overflow);
                page.lines.remove(lineNum);
            } else {
                pageOverflow.insert(0, currentLine.text.substring(splitAt) + overflow);
                currentLine.text = currentLine.text.substring(0, splitAt);
            }
            return pageOverflow.toString();
        }
        if (!overflow.isEmpty()) {
            return insert(page, lineNum + 1, 0, overflow, options);
        }
        return "";
    }

    public static Page pad(Page page) {
        Line lastLine = page.lines.get(page.lines.size() - 1);
        if (Line.getStringWidth(lastLine.wrappedFormatting + lastLine.text) < 116) {
            lastLine.text += "\n";
        }
        while (page.lines.size() < 13) {
            Line newLine = new Line();
            newLine.text = "\n";
            page.lines.add(newLine);
        }
        return page;
    }

    public static int totalCharacters(Page page) {
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
