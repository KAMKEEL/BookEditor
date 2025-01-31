package kamkeel.plugeditor.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kamkeel.plugeditor.FileHandler;
import kamkeel.plugeditor.Printer;
import kamkeel.plugeditor.book.Book;
import kamkeel.plugeditor.book.Line;
import kamkeel.plugeditor.book.Page;
import kamkeel.plugeditor.constants.Buttons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GuiPlugBook extends GuiScreen {
    private final Minecraft mc = Minecraft.getMinecraft();

    private int updateCount = 0;
    private int bookImageWidth = 192;
    private int bookImageHeight = 192;
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
    private static final String PAGE_INDICATOR_TEMPLATE = "Page %d of %d";

    private int selectedPageA = -1;
    private int selectedPageB = -1;

    // Formatting codes for buttons 50-71
    private static final String[] FORMAT_CODES = new String[]{
            "\u00a70", "\u00a71", "\u00a72", "\u00a73", "\u00a74", "\u00a75", "\u00a76", "\u00a77", "\u00a78", "\u00a79",
            "\u00a7a", "\u00a7b", "\u00a7c", "\u00a7d", "\u00a7e", "\u00a7f", "\u00a7k", "\u00a7l", "\u00a7m", "\u00a7n",
            "\u00a7o", "\u00a7r"
    };

    // GUI Buttons for book editing
    private GuiButton btnPasteBook;
    private GuiButton btnCutMultiplePages;
    private GuiButton btnSelectPageA;
    private GuiButton btnSelectPageB;
    private GuiButton btnCopySelectedPages;
    private GuiButton btnPasteMultiplePages;
    private GuiButton btnInsertPage;

    // Formatting buttons
    private GuiButton btnBlack;
    private GuiButton btnDarkBlue;
    private GuiButton btnDarkGreen;
    private GuiButton btnDarkAqua;
    private GuiButton btnDarkRed;
    private GuiButton btnDarkPurple;
    private GuiButton btnGold;
    private GuiButton btnGray;
    private GuiButton btnDarkGray;
    private GuiButton btnBlue;
    private GuiButton btnGreen;
    private GuiButton btnAqua;
    private GuiButton btnRed;
    private GuiButton btnLightPurple;
    private GuiButton btnYellow;
    private GuiButton btnWhite;
    private GuiButton btnObfuscated;
    private GuiButton btnBold;
    private GuiButton btnStrikethrough;
    private GuiButton btnUnderline;
    private GuiButton btnItalic;
    private GuiButton btnResetFormat;

    private NextPageButton btnNextPage;
    private NextPageButton btnPreviousPage;

    private Book book = new Book();
    private Book bookClipboard;
    private List<String> pageClipboard;
    private boolean heldBookIsWritable = false;
    private ItemStack mcBookObj;
    private FileHandler fileHandler = new FileHandler();

    // Constructor receives clipboard information for book and pages.
    public GuiPlugBook(Book _bookClipboard, List<String> _pageClipboard) {
        this.bookClipboard = _bookClipboard;
        this.pageClipboard = _pageClipboard;
        this.mcBookObj = this.mc.thePlayer.getHeldItem();
        if (this.mcBookObj.getItem().equals(Items.writable_book)) {
            this.heldBookIsWritable = true;
        }
        this.book = new Book();
        this.book.pages.add(new Page());

        // Load pages from the held book if available.
        if (this.mcBookObj.hasTagCompound()) {
            NBTTagCompound nbttagcompound = this.mcBookObj.getTagCompound();
            NBTTagList bookPages = nbttagcompound.getTagList("pages", 8);
            if (bookPages != null) {
                List<String> pages = new ArrayList<String>();
                for (int i = 0; i < bookPages.tagCount(); i++) {
                    pages.add(bookPages.getStringTagAt(i));
                }
                if (!pages.isEmpty()) {
                    this.book.clear();
                    this.book.insertPages(0, pages);
                }
            }
            if (!this.heldBookIsWritable) {
                String s = nbttagcompound.getString("author");
                if (!StringUtils.isNullOrEmpty(s))
                    this.book.author = s;
                s = nbttagcompound.getString("title");
                if (!StringUtils.isNullOrEmpty(s))
                    this.book.title = s;
            }
        }
    }

    // Sets the current book state based on an input book.
    public void setBook(Book inBook) {
        this.book.clone(inBook);
        this.book.cursorPage = 0;
        this.book.cursorLine = 0;
        this.book.cursorPosChars = 0;
    }

    // Helper methods to compute button positions for color and format buttons.
    public int getColorButX(int buttonNum) {
        int middle = this.width / 2;
        int leftMost = middle - 160;
        return leftMost + 20 * (buttonNum - Buttons.BTN_BLACK);
    }

    public int getFormatButX(int buttonNum) {
        int middle = this.width / 2;
        int leftMost = middle - 100;
        return leftMost + 20 * (buttonNum - Buttons.BTN_OBFUSCATED);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        int buttonWidth = 120;
        int buttonHeight = 20;
        int buttonSideOffset = 5;
        ScaledResolution scaledResolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        int rightXPos = scaledResolution.getScaledWidth() - buttonWidth + buttonSideOffset - 9;

        // Add buttons specific to writable books.
        if (this.heldBookIsWritable) {
            this.buttonList.add(new GuiButton(Buttons.BTN_SIGN, 5, 50, buttonWidth, buttonHeight, "Sign"));
            this.buttonList.add(new GuiButton(Buttons.BTN_DONE, 5, 75, buttonWidth, buttonHeight, "Done"));
            this.buttonList.add(this.btnCutMultiplePages = new GuiButton(Buttons.BTN_CUT_MULTIPLE_PAGES, rightXPos, 90, buttonWidth, buttonHeight, "Cut This Page"));
            this.buttonList.add(this.btnPasteBook = new GuiButton(Buttons.BTN_PASTE_BOOK, rightXPos, 25, buttonWidth, buttonHeight, "Paste Book"));
            this.buttonList.add(this.btnPasteMultiplePages = new GuiButton(Buttons.BTN_PASTE_MULTIPLE_PAGES, rightXPos, 110, buttonWidth, buttonHeight, "Paste Page"));
            this.buttonList.add(this.btnInsertPage = new GuiButton(Buttons.BTN_INSERT_PAGE, rightXPos, 135, buttonWidth, buttonHeight, "Insert Page"));
            this.buttonList.add(new GuiButton(Buttons.BTN_LOAD_BOOK, 5, 25, buttonWidth, buttonHeight, "Load Book"));

            // Add formatting buttons for colors and styles.
            int colorButY = this.height - 40;
            int formatButY = this.height - 20;
            this.buttonList.add(this.btnBlack = new GuiButton(Buttons.BTN_BLACK, getColorButX(Buttons.BTN_BLACK), colorButY, 20, 20, "\u00a70A"));
            this.buttonList.add(this.btnDarkBlue = new GuiButton(Buttons.BTN_DARK_BLUE, getColorButX(Buttons.BTN_DARK_BLUE), colorButY, 20, 20, "\u00a71A"));
            this.buttonList.add(this.btnDarkGreen = new GuiButton(Buttons.BTN_DARK_GREEN, getColorButX(Buttons.BTN_DARK_GREEN), colorButY, 20, 20, "\u00a72A"));
            this.buttonList.add(this.btnDarkAqua = new GuiButton(Buttons.BTN_DARK_AQUA, getColorButX(Buttons.BTN_DARK_AQUA), colorButY, 20, 20, "\u00a73A"));
            this.buttonList.add(this.btnDarkRed = new GuiButton(Buttons.BTN_DARK_RED, getColorButX(Buttons.BTN_DARK_RED), colorButY, 20, 20, "\u00a74A"));
            this.buttonList.add(this.btnDarkPurple = new GuiButton(Buttons.BTN_DARK_PURPLE, getColorButX(Buttons.BTN_DARK_PURPLE), colorButY, 20, 20, "\u00a75A"));
            this.buttonList.add(this.btnGold = new GuiButton(Buttons.BTN_GOLD, getColorButX(Buttons.BTN_GOLD), colorButY, 20, 20, "\u00a76A"));
            this.buttonList.add(this.btnGray = new GuiButton(Buttons.BTN_GRAY, getColorButX(Buttons.BTN_GRAY), colorButY, 20, 20, "\u00a77A"));
            this.buttonList.add(this.btnDarkGray = new GuiButton(Buttons.BTN_DARK_GRAY, getColorButX(Buttons.BTN_DARK_GRAY), colorButY, 20, 20, "\u00a78A"));
            this.buttonList.add(this.btnBlue = new GuiButton(Buttons.BTN_BLUE, getColorButX(Buttons.BTN_BLUE), colorButY, 20, 20, "\u00a79A"));
            this.buttonList.add(this.btnGreen = new GuiButton(Buttons.BTN_GREEN, getColorButX(Buttons.BTN_GREEN), colorButY, 20, 20, "\u00a7aA"));
            this.buttonList.add(this.btnAqua = new GuiButton(Buttons.BTN_AQUA, getColorButX(Buttons.BTN_AQUA), colorButY, 20, 20, "\u00a7bA"));
            this.buttonList.add(this.btnRed = new GuiButton(Buttons.BTN_RED, getColorButX(Buttons.BTN_RED), colorButY, 20, 20, "\u00a7cA"));
            this.buttonList.add(this.btnLightPurple = new GuiButton(Buttons.BTN_LIGHT_PURPLE, getColorButX(Buttons.BTN_LIGHT_PURPLE), colorButY, 20, 20, "\u00a7dA"));
            this.buttonList.add(this.btnYellow = new GuiButton(Buttons.BTN_YELLOW, getColorButX(Buttons.BTN_YELLOW), colorButY, 20, 20, "\u00a7eA"));
            this.buttonList.add(this.btnWhite = new GuiButton(Buttons.BTN_WHITE, getColorButX(Buttons.BTN_WHITE), colorButY, 20, 20, "\u00a7fA"));
            this.buttonList.add(this.btnObfuscated = new GuiButton(Buttons.BTN_OBFUSCATED, getFormatButX(Buttons.BTN_OBFUSCATED), formatButY, 20, 20, "#"));
            this.buttonList.add(this.btnBold = new GuiButton(Buttons.BTN_BOLD, getFormatButX(Buttons.BTN_BOLD), formatButY, 20, 20, "\u00a7lB"));
            this.buttonList.add(this.btnStrikethrough = new GuiButton(Buttons.BTN_STRIKETHROUGH, getFormatButX(Buttons.BTN_STRIKETHROUGH), formatButY, 20, 20, "\u00a7mS"));
            this.buttonList.add(this.btnUnderline = new GuiButton(Buttons.BTN_UNDERLINE, getFormatButX(Buttons.BTN_UNDERLINE), formatButY, 20, 20, "\u00a7nU"));
            this.buttonList.add(this.btnItalic = new GuiButton(Buttons.BTN_ITALIC, getFormatButX(Buttons.BTN_ITALIC), formatButY, 20, 20, "\u00a7oI"));
            this.buttonList.add(this.btnResetFormat = new GuiButton(Buttons.BTN_RESET_FORMAT, getFormatButX(Buttons.BTN_RESET_FORMAT), formatButY, 100, 20, "Reset Formatting"));
        } else {
            // Non-writable books only have a Done button.
            this.buttonList.add(new GuiButton(Buttons.BTN_DONE, this.width / 2 - 100, 4 + this.bookImageHeight, 200, 20, "Done"));
        }

        // Buttons common to both writable and non-writable books.
        this.buttonList.add(new GuiButton(Buttons.BTN_SAVE_BOOK, 5, 5, buttonWidth, buttonHeight, "Save Book"));
        this.buttonList.add(new GuiButton(Buttons.BTN_COPY_BOOK, rightXPos, 5, buttonWidth, buttonHeight, "Copy Book"));

        // Page selection buttons
        this.buttonList.add(this.btnSelectPageA = new GuiButton(Buttons.BTN_SELECT_PAGE_A, rightXPos, 50, buttonWidth / 2, buttonHeight, "A"));
        this.btnSelectPageA.visible = heldBookIsWritable;
        this.buttonList.add(this.btnSelectPageB = new GuiButton(Buttons.BTN_SELECT_PAGE_B, rightXPos + buttonWidth / 2, 50, buttonWidth / 2, buttonHeight, "B"));
        this.btnSelectPageB.visible = heldBookIsWritable;
        this.buttonList.add(this.btnCopySelectedPages = new GuiButton(Buttons.BTN_COPY_SELECTED_PAGES, rightXPos, 70, buttonWidth, buttonHeight, "Copy This Page"));

        int bookLeftSide = (this.width - this.bookImageWidth) / 2;
        this.buttonList.add(this.btnNextPage = new NextPageButton(Buttons.BTN_NEXT_PAGE, bookLeftSide + 120, 156, true));
        this.buttonList.add(this.btnPreviousPage = new NextPageButton(Buttons.BTN_PREVIOUS_PAGE, bookLeftSide + 38, 156, false));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        if (!this.book.isEmpty() && this.heldBookIsWritable) {
            this.book.sendBookToServer(false);
        }
    }

    @Override
    protected void actionPerformed(GuiButton buttonPressed) {
        if (!buttonPressed.enabled) {
            return;
        }
        List<String> blankPage;
        File signatureFile;
        Book signature;
        switch (buttonPressed.id) {
            case Buttons.BTN_NEXT_PAGE:
                // Prevent advancing past the last page.
                if (this.book.cursorPage < this.book.totalPages() - 1) {
                    this.book.turnPage(1);
                }
                break;
            case Buttons.BTN_PREVIOUS_PAGE:
                this.book.turnPage(-1);
                break;
            case Buttons.BTN_DONE:
                this.mc.displayGuiScreen(null);
                break;
            case Buttons.BTN_SIGN:
                if (this.heldBookIsWritable) {
                    // Truncate title and author if needed before signing.
                    this.book.title = Book.truncateStringChars(this.book.title, "..", 16, false);
                    this.book.author = Book.truncateStringChars(this.book.author, "..", 16, false);
                    this.mc.displayGuiScreen(new GuiSignPlugBook(this.book, this));
                }
                break;
            case Buttons.BTN_SAVE_BOOK:
                this.fileHandler.saveBookToGHBFile(this.book);
                break;
            case Buttons.BTN_LOAD_BOOK:
                this.mc.displayGuiScreen(new GuiFileBrowser(this));
                break;
            case Buttons.BTN_COPY_BOOK:
                this.bookClipboard.clone(this.book);
                break;
            case Buttons.BTN_PASTE_BOOK:
                if (this.bookClipboard != null) {
                    this.book.clone(this.bookClipboard);
                } else {
                    Printer.gamePrint(Printer.RED + "Clipboard is empty... Returning null...");
                }
                break;
            case Buttons.BTN_SELECT_PAGE_A:
                this.selectedPageA = this.book.cursorPage;
                break;
            case Buttons.BTN_SELECT_PAGE_B:
                this.selectedPageB = this.book.cursorPage;
                break;
            case Buttons.BTN_COPY_SELECTED_PAGES:
                if (this.selectedPageA != -1 && this.selectedPageB != -1
                        && this.selectedPageA >= 0 && this.selectedPageA <= this.selectedPageB
                        && this.selectedPageB < this.book.pages.size()) {
                    this.pageClipboard.clear();
                    this.pageClipboard.addAll(this.book.copyPages(this.selectedPageA, this.selectedPageB));
                } else {
                    this.pageClipboard.clear();
                    this.pageClipboard.addAll(this.book.copyPages(this.book.cursorPage, this.book.cursorPage));
                }
                break;
            case Buttons.BTN_PASTE_MULTIPLE_PAGES:
                this.book.insertPages(this.book.cursorPage, this.pageClipboard);
                break;
            case Buttons.BTN_INSERT_PAGE:
                blankPage = new ArrayList<String>();
                blankPage.add("");
                this.book.insertPages(this.book.cursorPage, blankPage);
                break;
            case Buttons.BTN_CUT_MULTIPLE_PAGES:
                if (this.selectedPageA != -1 && this.selectedPageB != -1
                        && this.selectedPageA >= 0 && this.selectedPageA <= this.selectedPageB
                        && this.selectedPageB < this.book.pages.size()) {
                    this.pageClipboard.clear();
                    this.pageClipboard.addAll(this.book.cutPages(this.selectedPageA, this.selectedPageB));
                } else {
                    this.pageClipboard.clear();
                    this.pageClipboard.addAll(this.book.cutPages(this.book.cursorPage, this.book.cursorPage));
                }
                break;
            case Buttons.BTN_ADD_SIGNATURE_PAGES:
                signatureFile = new File(this.fileHandler.getSignaturePath(), "default.ghb");
                signature = this.fileHandler.loadBook(signatureFile);
                if (signature != null) {
                    List<String> sigPages = new ArrayList<String>();
                    for (Page page : signature.pages) {
                        sigPages.add(page.asString());
                    }
                    this.book.insertPages(this.book.totalPages(), sigPages);
                } else {
                    Printer.gamePrint(Printer.RED + "Signature file couldn't be loaded!");
                }
                break;
            default:
                // Formatting buttons have ids 50-71
                if (buttonPressed.id >= Buttons.BTN_BLACK && buttonPressed.id <= Buttons.BTN_RESET_FORMAT) {
                    int pos = buttonPressed.id - Buttons.BTN_BLACK;
                    this.book.addTextAtCursor(FORMAT_CODES[pos]);
                } else {
                    System.out.println("#################### BUTTON NOT CODED YET!!!!!!!!");
                }
                break;
        }
    }

    @Override
    protected void mouseClicked(int posX, int posY, int button) {
        int bookLeftSide = (this.width - this.bookImageWidth) / 2;
        int bookTextLeft = bookLeftSide + 36;
        int bookTextTop = 33;
        if (posX >= bookTextLeft && posX < bookTextLeft + 116 && posY >= bookTextTop && posY < bookTextTop + 116) {
            int rowGuess = (posY - bookTextTop) / this.fontRendererObj.FONT_HEIGHT;
            Page currPage = this.book.pages.get(this.book.cursorPage);
            if (rowGuess < 0) {
                rowGuess = 0;
            } else if (rowGuess > currPage.lines.size() - 1) {
                rowGuess = currPage.lines.size() - 1;
            }
            this.book.cursorLine = rowGuess;
            Line currLine = currPage.lines.get(this.book.cursorLine);
            int xOffset = posX - bookTextLeft;
            if (xOffset < 0)
                xOffset = 0;
            int colGuess = Line.sizeStringToApproxWidthBlind(currLine.getTextWithWrappedFormatting(), xOffset);
            colGuess -= currLine.wrappedFormatting.length();
            if (colGuess < 0)
                colGuess = 0;
            if (colGuess > 0 && currLine.text.charAt(colGuess - 1) == '\n')
                colGuess--;
            this.book.cursorPosChars = colGuess;
        }
        super.mouseClicked(posX, posY, button);
    }

    @Override
    protected void keyTyped(char character, int keycode) {
        // Handle paste (CTRL+V)
        if (character == '\026' && this.heldBookIsWritable) {
            this.book.addTextAtCursor(GuiScreen.getClipboardString());
            return;
        }
        // Handle navigation and deletion keys
        switch (keycode) {
            case 203:
                this.book.moveCursor(Book.CursorDirection.LEFT);
                return;
            case 205:
                this.book.moveCursor(Book.CursorDirection.RIGHT);
                return;
            case 200:
                this.book.moveCursor(Book.CursorDirection.UP);
                return;
            case 208:
                this.book.moveCursor(Book.CursorDirection.DOWN);
                return;
            case 14:
                if (this.heldBookIsWritable) {
                    this.book.removeChar(false);
                }
                return;
            case 211:
                if (this.heldBookIsWritable) {
                    this.book.removeChar(true);
                }
                return;
            case 28:
            case 156:
                if (this.heldBookIsWritable) {
                    this.book.addTextAtCursor("\n");
                }
                return;
            case 1:
                this.mc.displayGuiScreen(null);
                return;
        }
        // Add character input if allowed.
        if (this.heldBookIsWritable && ChatAllowedCharacters.isAllowedCharacter(character)) {
            this.book.addTextAtCursor(Character.toString(character));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.updateCount++;
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        // Update paste buttons based on clipboard state.
        if (this.heldBookIsWritable) {
            this.btnPasteBook.enabled = !this.bookClipboard.pages.isEmpty();
            this.btnPasteMultiplePages.enabled = !this.pageClipboard.isEmpty();
        }
        if (this.heldBookIsWritable) {
            if (this.btnPasteMultiplePages.enabled) {
                this.btnPasteMultiplePages.displayString = "Paste " + this.pageClipboard.size() + " Page" +
                        ((this.pageClipboard.size() == 1) ? "" : "s");
            } else {
                this.btnPasteMultiplePages.displayString = "Paste Multiple";
            }
        }

        // Update page selection button text.
        updatePageSelectionButtons();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookGuiTextures);
        int bookLeftSide = (this.width - this.bookImageWidth) / 2;
        byte topOffset = 2;
        drawTexturedModalRect(bookLeftSide, topOffset, 0, 0, this.bookImageWidth, this.bookImageHeight);

        // Draw current page text.
        String currPageText = this.book.getCurrPageAsMCString();
        this.fontRendererObj.drawSplitString(currPageText, bookLeftSide + 36, topOffset + 32, 116, 0);

        // Draw page indicator.
        String pageIndicator = String.format(PAGE_INDICATOR_TEMPLATE, this.book.cursorPage + 1, this.book.totalPages());
        int pageIndicatorWidth = this.fontRendererObj.getStringWidth(pageIndicator);
        this.fontRendererObj.drawString(pageIndicator, bookLeftSide - pageIndicatorWidth + this.bookImageWidth - 44, topOffset + 16, 0);

        // Draw blinking cursor if writable.
        int cursorX1 = bookLeftSide + 35 + this.book.getCursorX();
        int cursorX2 = cursorX1 + 1;
        int cursorY1 = 33 + this.fontRendererObj.FONT_HEIGHT * this.book.cursorLine;
        int cursorY2 = cursorY1 + this.fontRendererObj.FONT_HEIGHT;
        byte phase = (byte) (this.updateCount / 10 % 2);
        int cursorColor = (phase == 1) ? -6645094 : -16777216;
        if (this.heldBookIsWritable) {
            drawRect(cursorX1, cursorY1, cursorX2, cursorY2, cursorColor);
        }
        super.drawScreen(par1, par2, par3);
    }

    // Update the text on the page selection and copy/cut buttons.
    private void updatePageSelectionButtons() {
        if (this.selectedPageA >= this.book.totalPages() || this.selectedPageB >= this.book.totalPages()) {
            this.selectedPageA = -1;
            this.selectedPageB = -1;
        }
        if (this.selectedPageA != -1 && this.selectedPageB != -1
                && this.selectedPageA >= 0 && this.selectedPageA <= this.selectedPageB
                && this.selectedPageB < this.book.totalPages()) {
            String xPages = (this.selectedPageB - this.selectedPageA + 1) + " Page" + ((this.selectedPageA != this.selectedPageB) ? "s" : "");
            this.btnCopySelectedPages.displayString = "Copy " + xPages;
            if (this.heldBookIsWritable) {
                this.btnCutMultiplePages.displayString = "Cut " + xPages;
            }
            this.btnSelectPageA.displayString = "A: " + (this.selectedPageA + 1);
            this.btnSelectPageB.displayString = "B: " + (this.selectedPageB + 1);
        } else if (this.selectedPageA != -1) {
            this.btnSelectPageA.displayString = "A: " + (this.selectedPageA + 1);
            this.btnCopySelectedPages.displayString = "Copy This Page";
            if (this.heldBookIsWritable) {
                this.btnCutMultiplePages.displayString = "Cut This Page";
            }
        } else if (this.selectedPageB != -1) {
            this.btnSelectPageB.displayString = "B: " + (this.selectedPageB + 1);
            this.btnCopySelectedPages.displayString = "Copy This Page";
            if (this.heldBookIsWritable) {
                this.btnCutMultiplePages.displayString = "Cut This Page";
            }
        } else {
            this.btnCopySelectedPages.displayString = "Copy This Page";
            if (this.heldBookIsWritable) {
                this.btnCutMultiplePages.displayString = "Cut This Page";
            }
            this.btnSelectPageA.displayString = "A";
            this.btnSelectPageB.displayString = "B";
        }
    }

    // Inner class for next/previous page buttons.
    static class NextPageButton extends GuiButton {
        private final boolean isRightArrow;

        public NextPageButton(int id, int x, int y, boolean isRightArrow) {
            super(id, x, y, 23, 13, "");
            this.isRightArrow = isRightArrow;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
                        && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(GuiPlugBook.bookGuiTextures);
                int textureX = 0;
                int textureY = 192;
                if (hovered) {
                    textureX += 23;
                }
                if (!this.isRightArrow) {
                    textureY += 13;
                }
                drawTexturedModalRect(this.xPosition, this.yPosition, textureX, textureY, 23, 13);
            }
        }
    }
}
