package kamkeel.bookeditor.book;

import kamkeel.bookeditor.book.format.FormattingOptions;
import kamkeel.bookeditor.book.format.FormattingUtil;

public class BookController {
    private final Book book;
    private final FormattingOptions options;

    public BookController(Book book) {
        this.book = book;
        this.options = FormattingOptions.defaults();
    }

    public FormattingOptions options() {
        return options;
    }

    public void removeChar(boolean nextChar) {
        if (book.pages.isEmpty()) {
            return;
        }
        Page currentPage = book.pages.get(book.cursorPage);
        if (currentPage.lines.isEmpty()) {
            return;
        }
        Line currentLine = currentPage.lines.get(book.cursorLine);
        if (nextChar) {
            handleForwardDelete(currentPage, currentLine);
        } else {
            handleBackspace(currentPage, currentLine);
        }
    }

    private void handleForwardDelete(Page currentPage, Line currentLine) {
        if (book.cursorPosChars < currentLine.text.length()) {
            int removeEnd = book.cursorPosChars;
            boolean removedFormatting = false;
            while (removeEnd < currentLine.text.length()) {
                int len = FormattingUtil.detectFormattingCodeLength(currentLine.text, removeEnd, options);
                if (len > 0) {
                    removeEnd += len;
                    removedFormatting = true;
                } else {
                    break;
                }
            }
            if (removedFormatting) {
                removeText(book.cursorPage, book.cursorLine, book.cursorPosChars, book.cursorPage, book.cursorLine, removeEnd);
            } else {
                removeText(book.cursorPage, book.cursorLine, book.cursorPosChars, book.cursorPage, book.cursorLine, book.cursorPosChars + 1);
            }
        } else if (currentLine.text.endsWith("\n")) {
            removeText(book.cursorPage, book.cursorLine, currentLine.text.length() - 1, book.cursorPage, book.cursorLine + 1, 0);
        } else if (book.cursorLine + 1 < currentPage.lines.size()) {
            int toLine = book.cursorLine + 1;
            if (currentPage.lines.get(toLine).text.length() >= 1) {
                removeText(book.cursorPage, book.cursorLine, book.cursorPosChars, book.cursorPage, toLine, 1);
            }
        } else if (book.cursorPage + 1 < book.pages.size()) {
            Page nextPage = book.pages.get(book.cursorPage + 1);
            if (nextPage.asString().isEmpty()) {
                book.pages.remove(book.cursorPage + 1);
            } else {
                removeText(book.cursorPage, book.cursorLine, book.cursorPosChars, book.cursorPage + 1, 0, 1);
            }
        }
    }

    private void handleBackspace(Page currentPage, Line currentLine) {
        if (book.cursorPosChars > 0) {
            int removeStart = book.cursorPosChars;
            boolean removedAny = false;
            while (removeStart > 0) {
                int codeStart = FormattingUtil.findFormattingCodeStart(currentLine.text, removeStart, options);
                if (codeStart >= 0) {
                    removedAny = true;
                    removeStart = codeStart;
                } else {
                    break;
                }
            }
            if (removedAny) {
                removeText(book.cursorPage, book.cursorLine, removeStart, book.cursorPage, book.cursorLine, book.cursorPosChars);
                book.cursorPosChars = Math.max(0, removeStart);
            } else {
                removeText(book.cursorPage, book.cursorLine, book.cursorPosChars - 1, book.cursorPage, book.cursorLine, book.cursorPosChars);
            }
        } else if (book.cursorLine > 0) {
            Line previous = currentPage.lines.get(book.cursorLine - 1);
            int removeIndex = Math.max(previous.text.length() - 1, 0);
            removeText(book.cursorPage, book.cursorLine - 1, removeIndex, book.cursorPage, book.cursorLine, book.cursorPosChars);
        } else if (book.cursorPage > 0) {
            Page prevPage = book.pages.get(book.cursorPage - 1);
            int lineNum = prevPage.lines.size() - 1;
            Line prevLine = prevPage.lines.get(lineNum);
            int removeIndex = Math.max(prevLine.text.length() - 1, 0);
            removeText(book.cursorPage - 1, lineNum, removeIndex, book.cursorPage, book.cursorLine, book.cursorPosChars);
        }
    }

    public void removeText(int fromPage, int fromLine, int fromChar, int toPage, int toLine, int toChar) {
        if (book.pages.isEmpty()) {
            return;
        }
        if (fromPage < 0) {
            fromPage = 0;
        }
        if (fromLine < 0) {
            fromLine = 0;
        }
        if (fromChar < 0) {
            fromChar = 0;
        }
        if (toPage >= book.pages.size()) {
            toPage = book.pages.size() - 1;
        }
        if (toPage < fromPage) {
            toPage = fromPage;
        }
        Page currPage = book.pages.get(toPage);
        if (toLine >= currPage.lines.size()) {
            toLine = currPage.lines.size() - 1;
        }
        if (toLine < fromLine && toPage == fromPage) {
            toLine = fromLine;
        }
        Line currLine = currPage.lines.get(toLine);
        if (toChar > currLine.text.length()) {
            toChar = currLine.text.length();
        }
        if (toChar < fromChar && toLine == fromLine && toPage == fromPage) {
            return;
        }
        String moveText = currLine.text.substring(toChar);
        int linePos = toLine;
        while (linePos++ < currPage.lines.size() - 1) {
            moveText = moveText + currPage.lines.get(linePos).text;
        }
        int pagePos = toPage;
        while (pagePos++ < book.pages.size() - 1) {
            moveText = moveText + book.pages.get(pagePos).asString();
        }
        pagePos = book.pages.size() - 1;
        while (pagePos > fromPage) {
            book.pages.remove(pagePos);
            pagePos--;
        }
        currPage = book.pages.get(fromPage);
        linePos = currPage.lines.size() - 1;
        while (linePos > fromLine) {
            currPage.lines.remove(linePos);
            linePos--;
        }
        currLine = currPage.lines.get(fromLine);
        currLine.text = currLine.text.substring(0, fromChar);
        addText(fromPage, fromLine, fromChar, moveText, false);
    }

    public void addTextAtCursor(String text) {
        addText(book.cursorPage, book.cursorLine, book.cursorPosChars, text, true);
    }

    public void addText(int pageNum, int lineNum, int charPos, String strAdd, boolean setCursorAfterInsertedText) {
        if (book.pages.isEmpty()) {
            book.pages.add(new Page());
        }
        if (pageNum > book.totalPages()) {
            pageNum = book.totalPages();
        } else if (pageNum < 0) {
            pageNum = 0;
        }
        if (pageNum == book.totalPages()) {
            book.pages.add(new Page());
        }
        int charsBeforeCursor = charPos;
        Page currentPage = book.pages.get(pageNum);
        for (int i = lineNum - 1; i >= 0; i--) {
            charsBeforeCursor += currentPage.lines.get(i).text.length();
        }
        for (int i = pageNum - 1; i >= 0; i--) {
            charsBeforeCursor += book.pages.get(i).asString().length();
        }
        if (setCursorAfterInsertedText) {
            charsBeforeCursor += strAdd.length();
        }
        currentPage = book.pages.get(pageNum);
        Line currentLine = currentPage.lines.get(lineNum);
        strAdd = currentLine.text.substring(0, charPos) + strAdd + currentLine.text.substring(charPos);
        if (lineNum > 0) {
            currentPage.lines.remove(lineNum);
            lineNum--;
            currentLine = currentPage.lines.get(lineNum);
            charPos = currentLine.text.length();
        } else if (pageNum > 0) {
            currentPage.lines.remove(lineNum);
            pageNum--;
            currentPage = book.pages.get(pageNum);
            lineNum = currentPage.lines.size() - 1;
            currentLine = currentPage.lines.get(lineNum);
            charPos = currentLine.text.length();
        } else {
            currentLine.text = "";
            charPos = 0;
        }
        while (!strAdd.isEmpty()) {
            if (pageNum == book.totalPages()) {
                book.pages.add(new Page());
            }
            strAdd = book.pages.get(pageNum).addText(lineNum, charPos, strAdd, options);
            pageNum++;
            lineNum = 0;
            charPos = 0;
        }
        repositionCursor(charsBeforeCursor);
    }

    private void repositionCursor(int charsBeforeCursor) {
        book.cursorPage = 0;
        Page currentPage = book.pages.get(book.cursorPage);
        while (charsBeforeCursor > currentPage.asString().length()) {
            charsBeforeCursor -= currentPage.asString().length();
            book.cursorPage++;
            currentPage = book.pages.get(book.cursorPage);
        }
        book.cursorLine = 0;
        Line currentLine = currentPage.lines.get(book.cursorLine);
        while (charsBeforeCursor > currentLine.text.length()) {
            charsBeforeCursor -= currentLine.text.length();
            book.cursorLine++;
            currentLine = currentPage.lines.get(book.cursorLine);
        }
        book.cursorPosChars = charsBeforeCursor;
        if (book.cursorPosChars > 0 && currentLine.text.charAt(book.cursorPosChars - 1) == '\n') {
            if (book.cursorLine < currentPage.lines.size() - 1) {
                book.cursorLine++;
                book.cursorPosChars = 0;
            } else if (book.cursorPage < book.totalPages() - 1) {
                book.cursorPage++;
                book.cursorLine = 0;
                book.cursorPosChars = 0;
            } else {
                book.cursorPosChars--;
            }
        }
    }

    public void moveCursor(Book.CursorDirection direction) {
        if (book.pages.isEmpty()) {
            return;
        }
        Page currentPage = book.pages.get(book.cursorPage);
        if (currentPage.lines.isEmpty()) {
            return;
        }
        Line currentLine = currentPage.lines.get(book.cursorLine);
        int cursorPosPx = getCursorX();
        switch (direction) {
            case UP:
                moveCursorUp(cursorPosPx);
                return;
            case DOWN:
                moveCursorDown(cursorPosPx);
                return;
            case LEFT:
                moveCursorLeft();
                return;
            case RIGHT:
                moveCursorRight();
                return;
        }
    }

    private void moveCursorUp(int cursorPosPx) {
        Page currentPage = book.pages.get(book.cursorPage);
        if (book.cursorLine == 0) {
            if (book.cursorPage > 0) {
                book.cursorPage--;
                currentPage = book.pages.get(book.cursorPage);
                book.cursorLine = currentPage.lines.size() - 1;
            } else {
                book.cursorPosChars = 0;
                return;
            }
        } else {
            book.cursorLine--;
        }
        Line currentLine = currentPage.lines.get(book.cursorLine);
        int cursorPosCharsUp = Line.sizeStringToApproxWidthBlind(currentLine.getTextWithWrappedFormatting(), cursorPosPx);
        book.cursorPosChars = Math.max(cursorPosCharsUp - currentLine.wrappedFormatting.length(), 0);
        adjustCursorForFormatting(currentLine);
    }

    private void moveCursorDown(int cursorPosPx) {
        Page currentPage = book.pages.get(book.cursorPage);
        if (book.cursorLine == currentPage.lines.size() - 1) {
            if (book.cursorPage < book.totalPages() - 1) {
                book.cursorPage++;
                book.cursorLine = 0;
            } else {
                Line lastLine = currentPage.lines.get(currentPage.lines.size() - 1);
                book.cursorPosChars = lastLine.text.length();
                return;
            }
        } else {
            book.cursorLine++;
        }
        currentPage = book.pages.get(book.cursorPage);
        Line currentLine = currentPage.lines.get(book.cursorLine);
        int cursorPosCharsDown = Line.sizeStringToApproxWidthBlind(currentLine.getTextWithWrappedFormatting(), cursorPosPx);
        book.cursorPosChars = Math.max(cursorPosCharsDown - currentLine.wrappedFormatting.length(), 0);
        adjustCursorForFormatting(currentLine);
    }

    private void moveCursorLeft() {
        Page currentPage = book.pages.get(book.cursorPage);
        Line currentLine = currentPage.lines.get(book.cursorLine);
        if (book.cursorPosChars > 0) {
            book.cursorPosChars--;
            int codeStart = FormattingUtil.findFormattingCodeStart(currentLine.text, book.cursorPosChars + 1, options);
            while (codeStart >= 0 && book.cursorPosChars >= codeStart + 1) {
                book.cursorPosChars = codeStart;
                codeStart = FormattingUtil.findFormattingCodeStart(currentLine.text, book.cursorPosChars, options);
            }
            if (book.cursorPosChars == 0 && book.cursorLine > 0) {
                book.cursorLine--;
                currentLine = currentPage.lines.get(book.cursorLine);
                book.cursorPosChars = currentLine.text.length();
                trimCursorIfOnNewline(currentLine);
            }
        } else if (book.cursorLine > 0) {
            book.cursorLine--;
            currentLine = currentPage.lines.get(book.cursorLine);
            book.cursorPosChars = currentLine.text.length();
            trimCursorIfOnNewline(currentLine);
        }
    }

    private void moveCursorRight() {
        Page currentPage = book.pages.get(book.cursorPage);
        Line currentLine = currentPage.lines.get(book.cursorLine);
        int lineLength = currentLine.text.length();
        if (book.cursorPosChars < lineLength) {
            // Skip any formatting codes directly at the cursor before moving
            while (book.cursorPosChars < lineLength) {
                int len = FormattingUtil.detectFormattingCodeLength(currentLine.text, book.cursorPosChars, options);
                if (len > 0) {
                    book.cursorPosChars += len;
                } else {
                    break;
                }
            }
            if (book.cursorPosChars < lineLength && currentLine.text.charAt(book.cursorPosChars) != '\n') {
                book.cursorPosChars++;
            } else if (book.cursorLine < currentPage.lines.size() - 1) {
                book.cursorLine++;
                book.cursorPosChars = 0;
                return;
            } else {
                return;
            }
            while (book.cursorPosChars < currentLine.text.length()) {
                int len = FormattingUtil.detectFormattingCodeLength(currentLine.text, book.cursorPosChars, options);
                if (len > 0) {
                    book.cursorPosChars += len;
                } else {
                    break;
                }
            }
        } else if (book.cursorLine < currentPage.lines.size() - 1) {
            book.cursorLine++;
            book.cursorPosChars = 0;
        }
    }

    private void adjustCursorForFormatting(Line currentLine) {
        trimCursorIfOnNewline(currentLine);
        int codeStart = FormattingUtil.findFormattingCodeStart(currentLine.text, book.cursorPosChars, options);
        while (codeStart >= 0) {
            book.cursorPosChars = Math.max(codeStart, 0);
            codeStart = FormattingUtil.findFormattingCodeStart(currentLine.text, book.cursorPosChars, options);
        }
    }

    private void trimCursorIfOnNewline(Line line) {
        if (book.cursorPosChars > 0 && book.cursorPosChars <= line.text.length()) {
            if (line.text.charAt(book.cursorPosChars - 1) == '\n') {
                book.cursorPosChars--;
            }
        }
    }

    public void turnPage(int numPages) {
        Page oldPage = book.pages.get(book.cursorPage);
        book.cursorPage += numPages;
        if (book.cursorPage < 0) {
            book.cursorPage = 0;
        } else if (book.cursorPage >= book.totalPages()) {
            book.cursorPage = book.totalPages();
            oldPage = Page.pad(oldPage);
            book.pages.add(new Page());
        }
        book.cursorLine = 0;
        book.cursorPosChars = 0;
    }

    public int getCursorX() {
        if (book.pages.isEmpty()) {
            return 0;
        }
        Page currentPage = book.pages.get(book.cursorPage);
        if (currentPage.lines.isEmpty()) {
            return 0;
        }
        Line currentLine = currentPage.lines.get(book.cursorLine);
        int visibleChars = Math.min(book.cursorPosChars, currentLine.text.length());
        return Line.getStringWidth(currentLine.getTextWithWrappedFormatting().substring(0, visibleChars + currentLine.wrappedFormatting.length()));
    }

    public String getCurrLine() {
        if (book.pages.isEmpty()) {
            return "";
        }
        return book.pages.get(book.cursorPage).lines.get(book.cursorLine).text;
    }

    public String getCurrLineWithWrappedFormatting() {
        if (book.pages.isEmpty()) {
            return "";
        }
        return book.pages.get(book.cursorPage).lines.get(book.cursorLine).getTextWithWrappedFormatting();
    }

    public String getCurrPageAsMCString() {
        if (book.cursorPage < 0) {
            book.cursorPage = 0;
            book.cursorLine = 0;
            book.cursorPosChars = 0;
        } else if (book.cursorPage >= book.totalPages()) {
            book.cursorPage = book.totalPages() - 1;
            if (book.cursorPage < 0) {
                book.cursorPage = 0;
            }
            Page lastPage = book.pages.get(book.cursorPage);
            book.cursorLine = lastPage.lines.size() - 1;
            if (book.cursorLine < 0) {
                book.cursorLine = 0;
            }
            Line lastLine = lastPage.lines.get(book.cursorLine);
            book.cursorPosChars = Math.max(lastLine.text.length() - 1, 0);
        }
        return book.pages.get(book.cursorPage).asString();
    }
}
