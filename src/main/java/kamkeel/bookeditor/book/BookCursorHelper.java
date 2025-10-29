package kamkeel.bookeditor.book;

import kamkeel.bookeditor.util.LineFormattingUtil;

/**
 * Cursor navigation logic extracted from {@link Book} for clarity and reuse.
 */
public final class BookCursorHelper {
    private BookCursorHelper() {
    }

    public static void moveCursor(Book book, Book.CursorDirection direction) {
        Page currPage = book.pages.get(book.cursorPage);
        Line currLine = currPage.lines.get(book.cursorLine);
        int cursorPosPx = book.getCursorX();

        switch (direction) {
            case UP:
                if (book.cursorLine == 0) {
                    if (book.cursorPage > 0) {
                        book.cursorPage--;
                        currPage = book.pages.get(book.cursorPage);
                        book.cursorLine = currPage.lines.size() - 1;
                    } else {
                        book.cursorPosChars = 0;
                        return;
                    }
                } else {
                    book.cursorLine--;
                }

                currLine = currPage.lines.get(book.cursorLine);
                int cursorPosCharsUp = LineFormattingUtil.sizeStringToApproxWidthBlind(
                    currLine.getTextWithWrappedFormatting(), cursorPosPx
                );
                book.cursorPosChars = Math.max(cursorPosCharsUp - currLine.wrappedFormatting.length(), 0);

                if (book.cursorPosChars > 0 && book.cursorPosChars <= currLine.text.length()) {
                    if (currLine.text.charAt(book.cursorPosChars - 1) == '\n') {
                        book.cursorPosChars--;
                    }
                }

                while (book.cursorPosChars > 0
                    && currLine.text.charAt(book.cursorPosChars - 1) == '\u00a7') {
                    book.cursorPosChars = Math.max(book.cursorPosChars - 2, 0);
                }
                return;
            case DOWN:
                if (book.cursorLine == currPage.lines.size() - 1) {
                    if (book.cursorPage < book.totalPages() - 1) {
                        book.cursorPage++;
                        book.cursorLine = 0;
                    } else {
                        currLine = currPage.lines.get(currPage.lines.size() - 1);
                        book.cursorPosChars = currLine.text.length();
                        return;
                    }
                } else {
                    book.cursorLine++;
                }

                currLine = currPage.lines.get(book.cursorLine);
                int cursorPosCharsDown = LineFormattingUtil.sizeStringToApproxWidthBlind(
                    currLine.getTextWithWrappedFormatting(), cursorPosPx
                );
                book.cursorPosChars = Math.max(cursorPosCharsDown - currLine.wrappedFormatting.length(), 0);

                if (book.cursorPosChars > 0 && book.cursorPosChars <= currLine.text.length()) {
                    if (currLine.text.charAt(book.cursorPosChars - 1) == '\n') {
                        book.cursorPosChars--;
                    }
                }

                while (book.cursorPosChars > 0
                    && currLine.text.charAt(book.cursorPosChars - 1) == '\u00a7') {
                    book.cursorPosChars = Math.max(book.cursorPosChars - 2, 0);
                }
                return;
            case LEFT:
                if (book.cursorPosChars > 0) {
                    book.cursorPosChars--;
                    while (book.cursorPosChars > 0
                        && currLine.text.charAt(book.cursorPosChars - 1) == '\u00a7') {
                        book.cursorPosChars = Math.max(book.cursorPosChars - 2, 0);
                    }
                    if (book.cursorPosChars == 0) {
                        if (book.cursorLine > 0) {
                            book.cursorLine--;
                            currLine = currPage.lines.get(book.cursorLine);
                            book.cursorPosChars = currLine.text.length();
                            if (book.cursorPosChars > 0
                                && currLine.text.charAt(book.cursorPosChars - 1) == '\n') {
                                book.cursorPosChars--;
                            }
                        }
                    }
                } else {
                    if (book.cursorLine > 0) {
                        book.cursorLine--;
                        currLine = currPage.lines.get(book.cursorLine);
                        book.cursorPosChars = currLine.text.length();
                        if (book.cursorPosChars > 0
                            && currLine.text.charAt(book.cursorPosChars - 1) == '\n') {
                            book.cursorPosChars--;
                        }
                    }
                }
                return;
            case RIGHT:
                int currLineLength = currLine.text.length();
                if (book.cursorPosChars < currLineLength
                    && currLine.text.charAt(book.cursorPosChars) != '\n') {
                    book.cursorPosChars++;

                    while (book.cursorPosChars < currLineLength
                        && currLine.text.charAt(book.cursorPosChars - 1) == '\u00a7') {
                        book.cursorPosChars += 1;
                        if (book.cursorPosChars < currLineLength) {
                            book.cursorPosChars++;
                        }
                    }
                } else {
                    if (book.cursorLine < currPage.lines.size() - 1) {
                        book.cursorLine++;
                        currLine = currPage.lines.get(book.cursorLine);
                        book.cursorPosChars = 0;
                        if (!currLine.text.isEmpty() && currLine.text.charAt(0) == '\n') {
                            book.cursorPosChars = Math.min(1, currLine.text.length());
                        }
                    } else if (book.cursorPage < book.totalPages() - 1) {
                        book.cursorPage++;
                        book.cursorLine = 0;
                        currPage = book.pages.get(book.cursorPage);
                        currLine = currPage.lines.get(0);
                        book.cursorPosChars = 0;
                        if (!currLine.text.isEmpty() && currLine.text.charAt(0) == '\n') {
                            book.cursorPosChars = Math.min(1, currLine.text.length());
                        }
                    }
                }
        }
    }
}
