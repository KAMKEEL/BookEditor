package kamkeel.bookeditor.book;

import kamkeel.bookeditor.BookController;
import kamkeel.bookeditor.util.FormattingUtil;
import kamkeel.bookeditor.util.LineFormattingUtil;

import java.util.ArrayList;

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

                book.cursorPosChars = skipFormattingBackward(currLine.text, book.cursorPosChars);
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

                book.cursorPosChars = skipFormattingBackward(currLine.text, book.cursorPosChars);
                return;
            case LEFT:
                if (book.cursorPosChars > 0) {
                    book.cursorPosChars--;
                    book.cursorPosChars = skipFormattingBackward(currLine.text, book.cursorPosChars);
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
                    book.cursorPosChars = skipFormattingForward(currLine.text, book.cursorPosChars);
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

    public static void placeCursorFromClick(Book book, int requestedLine, int xOffsetPixels) {
        if (book.pages.isEmpty()) {
            book.cursorPage = 0;
            book.cursorLine = 0;
            book.cursorPosChars = 0;
            return;
        }

        Page currPage = book.pages.get(book.cursorPage);
        if (currPage.lines.isEmpty()) {
            book.cursorLine = 0;
            book.cursorPosChars = 0;
            return;
        }

        int clampedLine = Math.max(0, Math.min(requestedLine, currPage.lines.size() - 1));
        book.cursorLine = clampedLine;
        Line currLine = currPage.lines.get(clampedLine);

        int targetPixels = Math.max(0, xOffsetPixels);
        String sanitized = BookController.getFormatter().stripColorCodes(currLine.text);
        if (sanitized == null) {
            sanitized = "";
        }

        ArrayList<Integer> positions = new ArrayList<Integer>();
        positions.add(0);
        for (int index = 0; index < currLine.text.length();) {
            int codeLength = FormattingUtil.detectFormattingCodeLength(currLine.text, index);
            if (codeLength > 0) {
                index += codeLength;
                continue;
            }
            index++;
            positions.add(index);
        }

        int accumulated = 0;
        int bestLogicalIndex = 0;
        int bestDistance = Math.abs(targetPixels - accumulated);
        for (int i = 0; i < sanitized.length(); i++) {
            char character = sanitized.charAt(i);
            int charWidth = character == '\n' ? 0 : LineFormattingUtil.getMetrics().charWidth(character);
            int nextAccumulated = accumulated + charWidth;

            int distanceToStart = Math.abs(targetPixels - accumulated);
            if (distanceToStart < bestDistance) {
                bestDistance = distanceToStart;
                bestLogicalIndex = i;
            }

            int distanceToEnd = Math.abs(targetPixels - nextAccumulated);
            if (distanceToEnd < bestDistance) {
                bestDistance = distanceToEnd;
                bestLogicalIndex = i + 1;
            }

            accumulated = nextAccumulated;
        }

        if (Math.abs(targetPixels - accumulated) < bestDistance) {
            bestLogicalIndex = sanitized.length();
        }

        if (bestLogicalIndex >= positions.size()) {
            bestLogicalIndex = positions.size() - 1;
        }

        int cursorChars = positions.get(bestLogicalIndex);
        cursorChars = Math.min(cursorChars, currLine.text.length());
        cursorChars = skipFormattingBackward(currLine.text, cursorChars);

        if (cursorChars > 0 && cursorChars <= currLine.text.length()) {
            if (currLine.text.charAt(cursorChars - 1) == '\n') {
                cursorChars--;
            }
        }

        book.cursorPosChars = Math.max(0, cursorChars);
    }

    private static int skipFormattingBackward(CharSequence text, int position) {
        int current = Math.max(0, position);
        while (current > 0) {
            int potentialStart = current - 1;
            int codeLength = FormattingUtil.detectFormattingCodeLength(text, potentialStart);
            if (codeLength > 0 && potentialStart + codeLength >= current) {
                current = potentialStart;
            } else {
                break;
            }
        }
        return Math.max(0, current);
    }

    private static int skipFormattingForward(CharSequence text, int position) {
        int length = text.length();
        int current = Math.max(0, position);
        int searchLimit = Math.min(length, current + 16);
        for (int search = current; search <= searchLimit; search++) {
            int start = FormattingUtil.findFormattingCodeStart(text, search);
            if (start >= 0 && start <= current) {
                current = start;
                break;
            }
        }
        boolean skippedAny = false;
        while (current < length) {
            int codeLength = FormattingUtil.detectFormattingCodeLength(text, current);
            if (codeLength > 0) {
                current += codeLength;
                skippedAny = true;
            } else {
                break;
            }
        }
        if (skippedAny && current < length) {
            current++;
        }
        return Math.min(current, length);
    }
}
