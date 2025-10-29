package kamkeel.bookeditor.book;

/**
 * Core data model representing a writable Minecraft book. The Book manages
 * pages, lines, cursor position and serialization to and from Minecraft's NBT
 * format. All editing operations funnel through this class.
 */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import kamkeel.bookeditor.FileHandler;
import kamkeel.bookeditor.Printer;
import kamkeel.bookeditor.util.AngelicaUtil;

public class Book {
    public static final String[] FORMAT_CODES = new String[]{
            "\u00a70", "\u00a71", "\u00a72", "\u00a73", "\u00a74", "\u00a75", "\u00a76", "\u00a77",
            "\u00a78", "\u00a79", "\u00a7a", "\u00a7b", "\u00a7c", "\u00a7d", "\u00a7e", "\u00a7f",
            "\u00a7k", "\u00a7l", "\u00a7m", "\u00a7n", "\u00a7o", "\u00a7r"
    };

    public List<Page> pages = new ArrayList<Page>();

    public String title = "";

    public String author = "";

    public enum CursorDirection {
        UP, DOWN, LEFT, RIGHT;
    }

    public int cursorPage = 0;

    public int cursorLine = 0;

    public int cursorPosChars = 0;

    public int totalPages() {
        return this.pages.size();
    }

    public void clear() {
        this.pages.clear();
        this.title = "";
        this.author = "";
        this.cursorPage = 0;
        this.cursorLine = 0;
        this.cursorPosChars = 0;
    }

    public void clone(Book inBook) {
        clear();
        this.title = inBook.title;
        this.author = inBook.author;
        for (Page inPage : inBook.pages) {
            this.pages.add(new Page());
            Page currPage = this.pages.get(this.pages.size() - 1);
            currPage.clear();
            for (Line inLine : inPage.lines) {
                currPage.lines.add(new Line());
                Line currLine = currPage.lines.get(currPage.lines.size() - 1);
                currLine.text = inLine.text;
                currLine.wrappedFormatting = inLine.wrappedFormatting;
            }
        }
    }

    public void dump() {
        String separator = "###############################################\n";
        for (int i = 0; i < totalPages(); i++) {
            System.out.println("\n" + separator + separator + "Page " + i);
            ((Page) this.pages.get(i)).dump();
        }
    }

    public List<String> cutPages(int firstPage, int lastPage) {
        if (lastPage < firstPage) {
            int temp = firstPage;
            firstPage = lastPage;
            lastPage = temp;
        }
        if (firstPage < 0)
            firstPage = 0;
        if (lastPage >= this.pages.size())
            lastPage = this.pages.size();
        List<String> cutPages = copyPages(firstPage, lastPage);
        for (int i = lastPage; i >= firstPage; i--)
            this.pages.remove(i);
        if (this.pages.isEmpty())
            this.pages.add(new Page());
        this.cursorLine = 0;
        this.cursorPosChars = 0;
        if (this.cursorPage > this.pages.size() - 1)
            this.cursorPage = this.pages.size() - 1;
        return cutPages;
    }

    public List<String> copyPages(int firstPage, int lastPage) {
        if (lastPage < firstPage) {
            int temp = firstPage;
            firstPage = lastPage;
            lastPage = temp;
        }
        if (firstPage < 0)
            firstPage = 0;
        if (lastPage >= this.pages.size())
            lastPage = this.pages.size();
        List<String> copiedPages = new ArrayList<String>();
        for (int i = firstPage; i <= lastPage; i++)
            copiedPages.add(((Page) this.pages.get(i)).asString());
        return copiedPages;
    }

    public void insertPages(int index, List<String> newPages) {
        if (index < 0)
            index = 0;
        if (index > this.pages.size())
            index = this.pages.size();
        int offset = 0;
        for (String pageText : newPages) {
            Page tempPage = new Page();
            tempPage.addText(0, 0, pageText);
            this.pages.add(index + offset, Page.pad(tempPage));
            offset++;
        }
        this.cursorLine = 0;
        this.cursorPosChars = 0;
    }

    public void removeChar(boolean nextChar) {
        // Retrieve current line
        Line currLine = this.pages.get(this.cursorPage).lines.get(this.cursorLine);

        if (nextChar) {
            // =============================
            // "Forward Delete" logic (Working)
            // =============================
            if (this.cursorPosChars < currLine.text.length()) {
                int removeEnd = this.cursorPosChars;
                boolean removedFormatting = false;

                // Scan forward to delete any Angelica formatting codes.
                while (removeEnd < currLine.text.length()) {
                    int codeLength = AngelicaUtil.detectAngelicaColorCodeLength(currLine.text, removeEnd);
                    if (codeLength > 0) {
                        removeEnd += codeLength;
                        removedFormatting = true;
                    } else {
                        break;
                    }
                }

                if (removedFormatting) {
                    removeText(this.cursorPage, this.cursorLine, this.cursorPosChars,
                        this.cursorPage, this.cursorLine, removeEnd);
                } else {
                    // Remove one normal character if no color codes were found
                    removeText(this.cursorPage, this.cursorLine, this.cursorPosChars,
                        this.cursorPage, this.cursorLine, this.cursorPosChars + 1);
                }
            } else if (currLine.text.endsWith("\n")) {
                // At the end of a line that ends with a newline. Delete the break.
                removeText(this.cursorPage, this.cursorLine, currLine.text.length() - 1,
                    this.cursorPage, this.cursorLine + 1, 0);
            } else if (this.cursorLine + 1 < this.pages.get(this.cursorPage).lines.size()) {
                int toLine = this.cursorLine + 1;
                if (this.pages.get(this.cursorPage).lines.get(toLine).text.length() >= 1) {
                    removeText(this.cursorPage, this.cursorLine, this.cursorPosChars,
                        this.cursorPage, toLine, 1);
                }
            } else if (this.cursorPage + 1 < this.pages.size()) {
                Page nextPage = this.pages.get(this.cursorPage + 1);
                if (nextPage.asString().isEmpty()) {
                    this.pages.remove(this.cursorPage + 1);
                } else {
                    removeText(this.cursorPage, this.cursorLine, this.cursorPosChars,
                        this.cursorPage + 1, 0, 1);
                }
            }
        }
        else {
            // =============================
            // "Backspace" logic (FIXED)
            // =============================
            if (this.cursorPosChars > 0) {
                int removeStart = this.cursorPosChars;
                boolean removedAny = false;

                while (removeStart > 0) {
                    int codeStart = AngelicaUtil.findAngelicaColorCodeStart(currLine.text, removeStart);
                    if (codeStart >= 0) {
                        removedAny = true;
                        removeStart = codeStart;
                    } else {
                        break;
                    }
                }

                if (removedAny) {
                    removeText(this.cursorPage, this.cursorLine, removeStart,
                        this.cursorPage, this.cursorLine, this.cursorPosChars);
                    this.cursorPosChars = Math.max(0, removeStart);
                } else {
                    removeText(cursorPage, cursorLine, cursorPosChars - 1, cursorPage, cursorLine, cursorPosChars);
                }
            }
            else if (this.cursorLine > 0) {
                // Backspace at the start of a line. Remove the character just
                // before the cursor which may be a newline inserted when the
                // user pressed Enter or simply the last character of the
                // previous line when wrapping occurred.
                currLine = this.pages.get(this.cursorPage).lines.get(this.cursorLine - 1);
                int removeIndex = currLine.text.length();
                if (removeIndex > 0)
                    removeIndex--; // Drop the preceding character/newline

                removeText(this.cursorPage, this.cursorLine - 1, removeIndex,
                    this.cursorPage, this.cursorLine, this.cursorPosChars);
            }
            else if (this.cursorPage > 0) {
                // Backspace at the start of the first line in the page: join with previous page
                Page currPage = this.pages.get(this.cursorPage - 1);
                int lineNum = currPage.lines.size() - 1;
                currLine = currPage.lines.get(lineNum);
                removeText(this.cursorPage - 1, lineNum, currLine.text.length(),
                    this.cursorPage, this.cursorLine, this.cursorPosChars);
            }
        }
    }




    public void removeText(int fromPage, int fromLine, int fromChar, int toPage, int toLine, int toChar) {
        if (fromPage < 0)
            fromPage = 0;
        if (fromLine < 0)
            fromLine = 0;
        if (fromChar < 0)
            fromChar = 0;
        if (toPage >= this.pages.size())
            toPage = this.pages.size() - 1;
        if (toPage < fromPage)
            toPage = fromPage;
        Page currPage = this.pages.get(toPage);
        if (toLine >= currPage.lines.size())
            toLine = currPage.lines.size() - 1;
        if (toLine < fromLine && toPage == fromPage)
            toLine = fromLine;
        Line currLine = currPage.lines.get(toLine);
        if (toChar > currLine.text.length())
            toChar = currLine.text.length();
        if (toChar < fromChar && toLine == fromLine && toPage == fromPage)
            return;
        String moveText = currLine.text.substring(toChar);
        int linePos = toLine;
        while (linePos++ < currPage.lines.size() - 1)
            moveText = moveText + ((Line) currPage.lines.get(linePos)).text;
        int pagePos = toPage;
        while (pagePos++ < this.pages.size() - 1)
            moveText = moveText + ((Page) this.pages.get(pagePos)).asString();
        pagePos = this.pages.size() - 1;
        while (pagePos > fromPage) {
            this.pages.remove(pagePos);
            pagePos--;
        }
        currPage = this.pages.get(fromPage);
        linePos = currPage.lines.size() - 1;
        while (linePos > fromLine) {
            currPage.lines.remove(linePos);
            linePos--;
        }
        currLine = currPage.lines.get(fromLine);
        currLine.text = currLine.text.substring(0, fromChar);
        addText(fromPage, fromLine, fromChar, moveText, false);
    }

    public void addTextAtCursor(String strAdd) {
        addText(this.cursorPage, this.cursorLine, this.cursorPosChars, strAdd, true);
    }

    public void addText(int pageNum, int lineNum, int charPos, String strAdd, boolean setCursorAfterInsertedText) {
        if (pageNum > totalPages()) {
            pageNum = totalPages();
        } else if (pageNum < 0) {
            pageNum = 0;
        }
        if (pageNum == totalPages())
            this.pages.add(new Page());
        int charsBeforeCursor = charPos;
        Page currPage = this.pages.get(pageNum);
        int i;
        for (i = lineNum - 1; i >= 0; i--)
            charsBeforeCursor += ((Line) currPage.lines.get(i)).text.length();
        for (i = pageNum - 1; i >= 0; i--)
            charsBeforeCursor += ((Page) this.pages.get(i)).asString().length();
        if (setCursorAfterInsertedText)
            charsBeforeCursor += strAdd.length();
        currPage = this.pages.get(pageNum);
        Line currLine = currPage.lines.get(lineNum);
        strAdd = currLine.text.substring(0, charPos) + strAdd + currLine.text.substring(charPos);
        if (lineNum > 0) {
            currPage.lines.remove(lineNum);
            lineNum--;
            currLine = currPage.lines.get(lineNum);
            charPos = currLine.text.length();
        } else if (pageNum > 0) {
            currPage.lines.remove(lineNum);
            pageNum--;
            currPage = this.pages.get(pageNum);
            lineNum = currPage.lines.size() - 1;
            currLine = currPage.lines.get(lineNum);
            charPos = currLine.text.length();
        } else {
            currLine.text = "";
            charPos = 0;
        }
        while (!strAdd.isEmpty()) {
            if (pageNum == totalPages())
                this.pages.add(new Page());
            strAdd = ((Page) this.pages.get(pageNum)).addText(lineNum, charPos, strAdd);
            pageNum++;
            lineNum = 0;
            charPos = 0;
        }
        this.cursorPage = 0;
        currPage = this.pages.get(this.cursorPage);
        while (charsBeforeCursor > currPage.asString().length()) {
            charsBeforeCursor -= currPage.asString().length();
            this.cursorPage++;
            currPage = this.pages.get(this.cursorPage);
        }
        this.cursorLine = 0;
        currLine = currPage.lines.get(this.cursorLine);
        while (charsBeforeCursor > currLine.text.length()) {
            charsBeforeCursor -= currLine.text.length();
            this.cursorLine++;
            currLine = currPage.lines.get(this.cursorLine);
        }
        this.cursorPosChars = charsBeforeCursor;
        if (this.cursorPosChars > 0 && currLine.text.charAt(this.cursorPosChars - 1) == '\n')
            if (this.cursorLine < currPage.lines.size() - 1) {
                this.cursorLine++;
                this.cursorPosChars = 0;
            } else if (this.cursorPage < totalPages() - 1) {
                this.cursorPage++;
                this.cursorLine = 0;
                this.cursorPosChars = 0;
            } else {
                this.cursorPosChars--;
            }
    }

    public void moveCursor(CursorDirection direction) {
        // Make sure we have a valid page & line
        Page currPage = this.pages.get(this.cursorPage);
        Line currLine = currPage.lines.get(this.cursorLine);

        // We'll often use pixel-based "cursorPosPx" for up/down arrow
        int cursorPosPx = getCursorX();

        switch (direction) {
            case UP:
                // Move cursor up one line
                if (this.cursorLine == 0) {
                    // Already on first line of page
                    if (this.cursorPage > 0) {
                        // Go to previous page if possible
                        this.cursorPage--;
                        currPage = this.pages.get(this.cursorPage);
                        this.cursorLine = currPage.lines.size() - 1;
                    } else {
                        // Can't move further up; clamp to pos=0
                        this.cursorPosChars = 0;
                        return;
                    }
                } else {
                    // Just go up one line
                    this.cursorLine--;
                }

                // Re-fetch the line now that we've changed cursorLine
                currLine = currPage.lines.get(this.cursorLine);

                // Approximate the cursor's character position via pixel
                int cursorPosCharsUp = Line.sizeStringToApproxWidthBlind(
                    currLine.wrappedFormatting + currLine.text, cursorPosPx
                );
                // Subtract out the wrappedFormatting so we land in the raw text
                this.cursorPosChars = Math.max(cursorPosCharsUp - currLine.wrappedFormatting.length(), 0);

                // If we landed just after a newline, step back 1 char
                if (this.cursorPosChars > 0 && this.cursorPosChars <= currLine.text.length()) {
                    if (currLine.text.charAt(this.cursorPosChars - 1) == '\n') {
                        this.cursorPosChars--;
                    }
                }

                // === Skip color codes (if we landed on them) ===
                while (this.cursorPosChars > 0
                    && currLine.text.charAt(this.cursorPosChars - 1) == '\u00a7') {
                    this.cursorPosChars = Math.max(this.cursorPosChars - 2, 0);
                }
                return;

            case DOWN:
                // Move cursor down one line
                if (this.cursorLine == currPage.lines.size() - 1) {
                    // If on last line of page, maybe go to next page
                    if (this.cursorPage < totalPages() - 1) {
                        this.cursorPage++;
                        this.cursorLine = 0;
                    } else {
                        // We're at very bottom, clamp to end
                        currLine = currPage.lines.get(currPage.lines.size() - 1);
                        this.cursorPosChars = currLine.text.length();
                        return;
                    }
                } else {
                    // Just go down one line
                    this.cursorLine++;
                }

                currLine = currPage.lines.get(this.cursorLine);

                // Approximate char position by pixel
                int cursorPosCharsDown = Line.sizeStringToApproxWidthBlind(
                    currLine.wrappedFormatting + currLine.text, cursorPosPx
                );
                this.cursorPosChars = Math.max(
                    cursorPosCharsDown - currLine.wrappedFormatting.length(), 0
                );

                // If on or just after a newline, step back 1
                if (this.cursorPosChars > 0 && this.cursorPosChars <= currLine.text.length()) {
                    if (currLine.text.charAt(this.cursorPosChars - 1) == '\n') {
                        this.cursorPosChars--;
                    }
                }

                // === Skip color codes ===
                while (this.cursorPosChars > 0
                    && currLine.text.charAt(this.cursorPosChars - 1) == '\u00a7') {
                    this.cursorPosChars = Math.max(this.cursorPosChars - 2, 0);
                }
                return;

            case LEFT:
                // Move cursor left by 1 displayed char
                if (this.cursorPosChars > 0) {
                    this.cursorPosChars--;

                    // === Skip color codes in case we land on them ===
                    while (this.cursorPosChars > 0
                        && currLine.text.charAt(this.cursorPosChars - 1) == '\u00a7') {
                        this.cursorPosChars = Math.max(this.cursorPosChars - 2, 0);
                    }

                    // Optional: if we end up at pos=0, jump to previous line
                    // if you want that behavior in one keypress.
                    // (Remove this if you prefer multiple left-presses)
                    if (this.cursorPosChars == 0) {
                        if (this.cursorLine > 0) {
                            // Move up a line, set pos to end
                            this.cursorLine--;
                            currLine = currPage.lines.get(this.cursorLine);
                            this.cursorPosChars = currLine.text.length();

                            // If that line ends with \n, step back 1
                            if (this.cursorPosChars > 0
                                && currLine.text.charAt(this.cursorPosChars - 1) == '\n') {
                                this.cursorPosChars--;
                            }
                        }
                    }
                } else {
                    // Already at 0, try moving up a line
                    if (this.cursorLine > 0) {
                        this.cursorLine--;
                        currLine = currPage.lines.get(this.cursorLine);

                        // Position at the end of that line
                        this.cursorPosChars = currLine.text.length();
                        if (this.cursorPosChars > 0
                            && currLine.text.charAt(this.cursorPosChars - 1) == '\n') {
                            this.cursorPosChars--;
                        }
                    }
                }
                return;

            case RIGHT:
                // Move cursor right by 1 displayed character
                int currLineLength = currLine.text.length();
                if (this.cursorPosChars < currLineLength
                    && currLine.text.charAt(this.cursorPosChars) != '\n') {
                    this.cursorPosChars++;

                    // === Skip color codes in case there's a chain ===
                    // We need a loop that keeps going if we still find '§'
                    // at the new position (minus 1).
                    while (this.cursorPosChars < currLineLength
                        && currLine.text.charAt(this.cursorPosChars - 1) == '\u00a7') {
                        // Skip '§' + next char
                        this.cursorPosChars += 1;
                        if (this.cursorPosChars < currLineLength) {
                            this.cursorPosChars++;
                        }
                    }
                } else {
                    // If we're at the end or on a newline, try next line
                    if (this.cursorLine < currPage.lines.size() - 1) {
                        this.cursorLine++;
                        this.cursorPosChars = 0;
                    }
                }
                return;
        }
    }


    public void turnPage(int numPages) {
        Page oldPage = this.pages.get(this.cursorPage);
        this.cursorPage += numPages;
        if (this.cursorPage < 0) {
            this.cursorPage = 0;
        } else if (this.cursorPage >= totalPages()) {
            this.cursorPage = totalPages();
            oldPage = Page.pad(oldPage);
            this.pages.add(new Page());
        }
        this.cursorLine = 0;
        this.cursorPosChars = 0;
    }

    public int getCursorX() {
        if (this.pages.isEmpty())
            return 0;
        Line currLine = ((Page) this.pages.get(this.cursorPage)).lines.get(this.cursorLine);
        int visibleChars = Math.min(this.cursorPosChars, currLine.text.length());
        return Line.getStringWidth(currLine.getTextWithWrappedFormatting().substring(0, visibleChars + currLine.wrappedFormatting.length()));
    }

    public String getCurrLine() {
        return ((Line) ((Page) this.pages.get(this.cursorPage)).lines.get(this.cursorLine)).text;
    }

    public String getCurrLineWithWrappedFormatting() {
        return ((Line) ((Page) this.pages.get(this.cursorPage)).lines.get(this.cursorLine)).getTextWithWrappedFormatting();
    }

    public String getCurrPageAsMCString() {
        if (this.cursorPage < 0) {
            System.out.println("GBook.cursorPage was less than zero (" + this.cursorPage + "... This should never happen...");
            this.cursorPage = 0;
            this.cursorLine = 0;
            this.cursorPosChars = 0;
        } else if (this.cursorPage >= totalPages()) {
            this.cursorPage = totalPages() - 1;
            if (this.cursorPage < 0)
                this.cursorPage = 0;
            Page lastPage = this.pages.get(this.cursorPage);
            this.cursorLine = lastPage.lines.size() - 1;
            if (this.cursorLine < 0)
                this.cursorLine = 0;
            Line lastLine = lastPage.lines.get(this.cursorLine);
            this.cursorPosChars = lastLine.text.length() - 1;
            if (this.cursorPosChars < 0)
                this.cursorPosChars = 0;
        }
        return ((Page) this.pages.get(this.cursorPage)).asString();
    }

    public boolean isEmpty() {
        for (Page page : this.pages) {
            if (!page.isEmpty())
                return false;
        }
        return true;
    }

    public void sendBookToServer(boolean signIt) {
        System.out.println("Sending book to server!");
        Minecraft minecraft = Minecraft.getMinecraft();
        ItemStack bookObj = AngelicaUtil.safeGetHeldItem(minecraft);

        if (!AngelicaUtil.isWritableBook(bookObj)) {
            Printer.gamePrint(Printer.RED + "You must hold a writable book to save.");
            return;
        }

        if (totalPages() > 0) {
            for (int i = totalPages() - 1; i >= 0 && (
                    (Page) this.pages.get(i)).asString().isEmpty(); i--) {
                if (i == 0) {
                    System.out.println("Can't save an empty book! Aborting!");
                    return;
                }
                this.pages.remove(i);
                if (this.cursorPage >= totalPages()) {
                    this.cursorPage = totalPages() - 1;
                    this.cursorLine = 0;
                    this.cursorPosChars = 0;
                }
            }
            NBTTagList bookPages = new NBTTagList();
            int pageCount = 0;
            for (Page page : this.pages) {
                pageCount++;
                if (pageCount == 51) {
                    Printer.gamePrint(Printer.DARK_RED + "Book is over 50 pages. Sending the first 50 to the server, and automatically saving the book...");
                    FileHandler fh = new FileHandler();
                    fh.saveBookToGHBFile(this);
                    break;
                }
                String pageText = AngelicaUtil.sanitizeAngelicaFormatting(page.asString());
                bookPages.appendTag((NBTBase) new NBTTagString(pageText));
            }
            if (bookObj.hasTagCompound()) {
                NBTTagCompound nbttagcompound = bookObj.getTagCompound();
                nbttagcompound.setTag("pages", (NBTBase) bookPages);
            } else {
                bookObj.setTagInfo("pages", (NBTBase) bookPages);
            }
            String sendMode = "MC|BEdit";
            if (signIt) {
                sendMode = "MC|BSign";
                bookObj.setTagInfo("author", (NBTBase) new NBTTagString(this.author));
                bookObj.setTagInfo("title", (NBTBase) new NBTTagString(this.title));
                bookObj.func_150996_a(Items.written_book);
            }
            ByteBuf bytebuf = Unpooled.buffer();
            try {
                (new PacketBuffer(bytebuf)).writeItemStackToBuffer(bookObj);
                Minecraft.getMinecraft().getNetHandler().addToSendQueue((Packet) new C17PacketCustomPayload(sendMode, bytebuf));
            } catch (Exception exception) {
                System.out.println("Couldn't send book info:\n" + exception.getMessage());
                Printer.gamePrint(Printer.RED + "Failed to send book to server.");
            } finally {
                bytebuf.release();
            }
        }
    }

    public static String truncateStringChars(String strIn, String substituteChars, int maxChars, boolean keepRightSide) {
        if (strIn.length() <= maxChars)
            return strIn;
        strIn = strIn.replaceAll(" ", "");
        if (strIn.length() <= maxChars)
            return strIn;
        if (keepRightSide) {
            strIn = substituteChars + strIn.substring(strIn.length() - maxChars - substituteChars.length(), strIn.length());
        } else {
            strIn = strIn.substring(0, maxChars - substituteChars.length()) + substituteChars;
        }
        return strIn;
    }

    public static String truncateStringPixels(String strIn, String substituteChars, int maxWidth, boolean keepRightSide) {
        FontRenderer f = (Minecraft.getMinecraft()).fontRenderer;
        if (f.getStringWidth(strIn) <= maxWidth)
            return strIn;
        String strOut = "";
        int subCharsWidth = f.getStringWidth(substituteChars);
        int startPos = 0;
        int endPos = strIn.length() - 1;
        int direction = 1;
        if (keepRightSide) {
            startPos = strIn.length() - 1;
            endPos = -1;
            direction = -1;
        }
        for (int i = startPos; i != endPos; i += direction) {
            char c = strIn.charAt(i);
            if (f.getStringWidth(c + strOut) + subCharsWidth > maxWidth) {
                break;
            }
            if (keepRightSide) {
                strOut = c + strOut;
            } else {
                strOut = strOut + c;
            }
        }
        if (keepRightSide)
            return substituteChars + strOut;
        return strOut + substituteChars;
    }

    public static String removeFormatting(String strIn) {
        return strIn.replaceAll("\u00a7[0-9a-fA-Fk-oK-OrR]?", "");
    }
}
