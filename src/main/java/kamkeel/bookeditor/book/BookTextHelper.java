package kamkeel.bookeditor.book;

import kamkeel.bookeditor.util.FormattingUtil;

/**
 * Utility methods containing the heavy text manipulation logic for {@link Book}.
 * Extracting these routines keeps the main data class focused on state while
 * still exposing the same behaviour to the rest of the mod.
 */
public final class BookTextHelper {
    private BookTextHelper() {
    }

    public static void removeChar(Book book, boolean nextChar) {
        Line currLine = book.pages.get(book.cursorPage).lines.get(book.cursorLine);

        if (nextChar) {
            if (book.cursorPosChars < currLine.text.length()) {
                int removeEnd = book.cursorPosChars;
                boolean removedFormatting = false;

                while (removeEnd < currLine.text.length()) {
                    int codeLength = FormattingUtil.detectFormattingCodeLength(currLine.text, removeEnd);
                    if (codeLength > 1) {
                        removeEnd += codeLength;
                        removedFormatting = true;
                    } else {
                        break;
                    }
                }

                if (removedFormatting) {
                    removeText(book, book.cursorPage, book.cursorLine, book.cursorPosChars,
                        book.cursorPage, book.cursorLine, removeEnd);
                } else {
                    removeText(book, book.cursorPage, book.cursorLine, book.cursorPosChars,
                        book.cursorPage, book.cursorLine, book.cursorPosChars + 1);
                }
            } else if (currLine.text.endsWith("\n")) {
                removeText(book, book.cursorPage, book.cursorLine, currLine.text.length() - 1,
                    book.cursorPage, book.cursorLine + 1, 0);
            } else if (book.cursorLine + 1 < book.pages.get(book.cursorPage).lines.size()) {
                int toLine = book.cursorLine + 1;
                if (book.pages.get(book.cursorPage).lines.get(toLine).text.length() >= 1) {
                    removeText(book, book.cursorPage, book.cursorLine, book.cursorPosChars,
                        book.cursorPage, toLine, 1);
                }
            } else if (book.cursorPage + 1 < book.pages.size()) {
                Page nextPage = book.pages.get(book.cursorPage + 1);
                if (nextPage.asString().isEmpty()) {
                    book.pages.remove(book.cursorPage + 1);
                } else {
                    removeText(book, book.cursorPage, book.cursorLine, book.cursorPosChars,
                        book.cursorPage + 1, 0, 1);
                }
            }
        } else {
            if (book.cursorPosChars > 0) {
                int removeStart = book.cursorPosChars;
                boolean removedAny = false;

                while (removeStart > 0) {
                    int codeStart = FormattingUtil.findFormattingCodeStart(currLine.text, removeStart);
                    if (codeStart >= 0) {
                        removedAny = true;
                        removeStart = codeStart;
                    } else {
                        break;
                    }
                }

                if (removedAny) {
                    removeText(book, book.cursorPage, book.cursorLine, removeStart,
                        book.cursorPage, book.cursorLine, book.cursorPosChars);
                    book.cursorPosChars = Math.max(0, removeStart);
                } else {
                    removeText(book, book.cursorPage, book.cursorLine, book.cursorPosChars - 1,
                        book.cursorPage, book.cursorLine, book.cursorPosChars);
                }
            } else if (book.cursorLine > 0) {
                currLine = book.pages.get(book.cursorPage).lines.get(book.cursorLine - 1);
                int removeIndex = currLine.text.length();
                if (removeIndex > 0) {
                    removeIndex--;
                }
                removeText(book, book.cursorPage, book.cursorLine - 1, removeIndex,
                    book.cursorPage, book.cursorLine, book.cursorPosChars);
            } else if (book.cursorPage > 0) {
                Page currPage = book.pages.get(book.cursorPage - 1);
                int lineNum = currPage.lines.size() - 1;
                currLine = currPage.lines.get(lineNum);
                removeText(book, book.cursorPage - 1, lineNum, currLine.text.length(),
                    book.cursorPage, book.cursorLine, book.cursorPosChars);
            }
        }
    }

    public static void removeText(Book book, int fromPage, int fromLine, int fromChar, int toPage, int toLine, int toChar) {
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
        addText(book, fromPage, fromLine, fromChar, moveText, false);
    }

    public static void addTextAtCursor(Book book, String strAdd) {
        addText(book, book.cursorPage, book.cursorLine, book.cursorPosChars, strAdd, true);
    }

    public static void addText(Book book, int pageNum, int lineNum, int charPos, String strAdd, boolean setCursorAfterInsertedText) {
        if (pageNum > book.totalPages()) {
            pageNum = book.totalPages();
        } else if (pageNum < 0) {
            pageNum = 0;
        }
        if (pageNum == book.totalPages()) {
            book.pages.add(new Page());
        }
        int charsBeforeCursor = charPos;
        Page currPage = book.pages.get(pageNum);
        for (int i = lineNum - 1; i >= 0; i--) {
            charsBeforeCursor += currPage.lines.get(i).text.length();
        }
        for (int i = pageNum - 1; i >= 0; i--) {
            charsBeforeCursor += book.pages.get(i).asString().length();
        }
        if (setCursorAfterInsertedText && strAdd != null) {
            charsBeforeCursor += strAdd.length();
        }
        currPage = book.pages.get(pageNum);
        Line currLine = currPage.lines.get(lineNum);
        String currentText = currLine.text;
        if (strAdd == null) {
            strAdd = "";
        }
        strAdd = currentText.substring(0, Math.min(charPos, currentText.length())) + strAdd
            + currentText.substring(Math.min(charPos, currentText.length()));
        if (lineNum > 0 && charPos > 0) {
            currPage.lines.remove(lineNum);
            lineNum--;
            currLine = currPage.lines.get(lineNum);
            charPos = currLine.text.length();
        } else if (pageNum > 0 && charPos > 0) {
            currPage.lines.remove(lineNum);
            pageNum--;
            currPage = book.pages.get(pageNum);
            lineNum = currPage.lines.size() - 1;
            currLine = currPage.lines.get(lineNum);
            charPos = currLine.text.length();
        } else {
            currLine.text = "";
            charPos = 0;
        }
        while (!strAdd.isEmpty()) {
            if (pageNum == book.totalPages()) {
                book.pages.add(new Page());
            }
            strAdd = book.pages.get(pageNum).addText(lineNum, charPos, strAdd);
            pageNum++;
            lineNum = 0;
            charPos = 0;
        }
        book.cursorPage = 0;
        currPage = book.pages.get(book.cursorPage);
        while (charsBeforeCursor > currPage.asString().length()) {
            charsBeforeCursor -= currPage.asString().length();
            book.cursorPage++;
            currPage = book.pages.get(book.cursorPage);
        }
        book.cursorLine = 0;
        currLine = currPage.lines.get(book.cursorLine);
        while (charsBeforeCursor > currLine.text.length()) {
            charsBeforeCursor -= currLine.text.length();
            book.cursorLine++;
            currLine = currPage.lines.get(book.cursorLine);
        }
        book.cursorPosChars = charsBeforeCursor;
        if (book.cursorPosChars > 0 && currLine.text.charAt(book.cursorPosChars - 1) == '\n') {
            advanceCursorPastNewline(book, currPage, currLine);
        }
    }

    private static void advanceCursorPastNewline(Book book, Page currPage, Line currLine) {
        if (currLine.text.length() == 1 && currLine.text.charAt(0) == '\n'
            && book.cursorLine < currPage.lines.size() - 1) {
            book.cursorPosChars = 0;
            return;
        }
        if (book.cursorLine < currPage.lines.size() - 1) {
            book.cursorLine++;
            book.cursorPosChars = 0;
            return;
        }

        if (book.cursorPage < book.totalPages() - 1) {
            book.cursorPage++;
            book.cursorLine = 0;
            book.cursorPosChars = 0;
            return;
        }

        if (currPage.lines.size() < MAX_LINES_PER_PAGE) {
            Line newLine = new Line();
            newLine.wrappedFormatting = currLine.getActiveFormatting();
            currPage.lines.add(newLine);
            book.cursorLine++;
            book.cursorPosChars = 0;
            return;
        }

        Page newPage = new Page();
        Line firstLine = newPage.lines.get(0);
        firstLine.wrappedFormatting = currLine.getActiveFormatting();
        book.pages.add(newPage);
        book.cursorPage = book.pages.size() - 1;
        book.cursorLine = 0;
        book.cursorPosChars = 0;
    }

    private static final int MAX_LINES_PER_PAGE = 13;
}
