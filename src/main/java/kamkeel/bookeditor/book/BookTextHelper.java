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
        if (lineNum > 0) {
            currPage.lines.remove(lineNum);
            lineNum--;
            currLine = currPage.lines.get(lineNum);
            charPos = currLine.text.length();
        } else if (pageNum > 0) {
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

        String pageText = currPage.asString();
        int substringEnd = Math.min(charsBeforeCursor, pageText.length());
        String uptoCursor = pageText.substring(0, substringEnd);
        int newlineCount = 0;
        int lastNewlineIndex = -1;
        for (int i = 0; i < uptoCursor.length(); i++) {
            if (uptoCursor.charAt(i) == '\n') {
                newlineCount++;
                lastNewlineIndex = i;
            }
        }

        int targetLineIndex = newlineCount;
        int targetPos = substringEnd;
        if (lastNewlineIndex >= 0) {
            targetPos = substringEnd - lastNewlineIndex - 1;
        }

        while (targetLineIndex >= currPage.lines.size()) {
            if (currPage.lines.size() >= 13) {
                Line previous = currPage.lines.get(currPage.lines.size() - 1);
                Page newPage = new Page();
                newPage.lines.clear();
                Line firstLine = new Line();
                firstLine.wrappedFormatting = previous.getActiveFormatting();
                newPage.lines.add(firstLine);
                book.pages.add(newPage);
                book.cursorPage = book.pages.size() - 1;
                book.cursorLine = 0;
                book.cursorPosChars = 0;
                return;
            }
            Line previous = currPage.lines.get(currPage.lines.size() - 1);
            Line extra = new Line();
            extra.wrappedFormatting = previous.getActiveFormatting();
            currPage.lines.add(extra);
        }

        book.cursorLine = targetLineIndex;
        currLine = currPage.lines.get(book.cursorLine);
        book.cursorPosChars = Math.min(targetPos, currLine.text.length());
    }
}
