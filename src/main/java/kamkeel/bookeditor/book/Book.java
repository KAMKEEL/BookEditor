package kamkeel.bookeditor.book;

/**
 * Core data model representing a writable Minecraft book. The Book manages
 * pages, lines, cursor position and serialization to and from Minecraft's NBT
 * format. All editing operations funnel through this class.
 */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kamkeel.bookeditor.FileHandler;
import kamkeel.bookeditor.Printer;
import kamkeel.bookeditor.controller.BookController;
import kamkeel.bookeditor.util.BookItemUtil;
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

import java.util.ArrayList;
import java.util.List;

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
        BookTextHelper.removeChar(this, nextChar);
    }


    public void removeText(int fromPage, int fromLine, int fromChar, int toPage, int toLine, int toChar) {
        BookTextHelper.removeText(this, fromPage, fromLine, fromChar, toPage, toLine, toChar);
    }

    public void addTextAtCursor(String strAdd) {
        BookTextHelper.addTextAtCursor(this, strAdd);
    }

    public void addText(int pageNum, int lineNum, int charPos, String strAdd, boolean setCursorAfterInsertedText) {
        BookTextHelper.addText(this, pageNum, lineNum, charPos, strAdd, setCursorAfterInsertedText);
    }

    public void moveCursor(CursorDirection direction) {
        BookCursorHelper.moveCursor(this, direction);
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
        ItemStack bookObj = BookItemUtil.safeGetHeldItem(minecraft);

        if (!BookItemUtil.isWritableBook(bookObj)) {
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
                String pageText = BookController.getFormatter().sanitizeFormatting(page.asString());
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
