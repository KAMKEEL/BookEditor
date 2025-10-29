package kamkeel.bookeditor.gui;

/**
 * Simple file browser used for selecting books to load or preview. It displays
 * a scrollable list of files and directories from the configured book folder.
 */

import kamkeel.bookeditor.FileHandler;
import kamkeel.bookeditor.book.Book;
import kamkeel.bookeditor.book.Page;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;

import java.io.File;
import java.util.List;

public class GuiFileBrowser extends GuiScreen {
    private List<File> listItems;

    private ScrollList scrollList;

    public int slotSelected = -1;

    private static final int BUTTONWIDTH = 60;

    private static final int BUTTONHEIGHT = 20;

    private static final int BTN_LOAD = 0;

    private static final int BTN_CANCEL = 1;

    private GuiButton btnLoad;

    private GuiButton btnCancel;

    private FileHandler fileHandler = new FileHandler();

    private GuiBookEditor parentScreen;

    private Book tempBook;

    private String displayPath = "";

    private String previewTitle = "";

    private String previewAuthor = "";

    private String previewPage = "";

    public GuiFileBrowser(GuiBookEditor _parentScreen) {
        this.parentScreen = _parentScreen;
    }

    public void initGui() {
        this.fileHandler.currentPath = this.fileHandler.getDefaultPath();
        this.displayPath = this.fileHandler.currentPath.getAbsolutePath();
        this.buttonList.add(this.btnLoad = new GuiButton(0, this.width - 65, this.height - 50, 60, 20, "Load"));
        this.buttonList.add(this.btnCancel = new GuiButton(1, this.width - 65, this.height - 25, 60, 20, "Cancel"));
        int rootNum = 100;
        List<File> roots = this.fileHandler.getValidRoots();
        for (File root : roots) {
            this.buttonList.add(new GuiButton(rootNum, 5, 35 + 21 * (rootNum - 100), 50, 20, root.getAbsolutePath()));
            rootNum++;
        }
        populateFileList();
        this.scrollList = new ScrollList();
        this.scrollList.registerScrollButtons(4, 5);
    }

    private void populateFileList() {
        this.listItems = this.fileHandler.listFiles(this.fileHandler.currentPath);
    }

    private void loadPreview(File file) {
        this.tempBook = this.fileHandler.loadBook(file);
        if (this.tempBook != null) {
            this.previewAuthor = this.tempBook.author;
            this.previewTitle = this.tempBook.title;
            String firstPage = Book.removeFormatting(((Page) this.tempBook.pages.get(0)).asString().replaceAll("\n", " "));
            this.previewPage = Book.truncateStringPixels(firstPage, "...", 200, false);
        } else {
            this.previewTitle = "";
            this.previewAuthor = "";
            this.previewPage = "";
        }
    }

    private void goBackToParentGui() {
        this.mc.displayGuiScreen(this.parentScreen);
    }

    protected void keyTyped(char par1, int par2) {
        if (par2 == 1)
            goBackToParentGui();
    }

    public void drawScreen(int par1, int par2, float par3) {
        this.displayPath = this.fileHandler.currentPath.getAbsolutePath();
        this.btnLoad.enabled = (this.tempBook != null);
        populateFileList();
        this.scrollList.drawScreen(par1, par2, par3);
        super.drawScreen(par1, par2, par3);
        drawCenteredString(this.fontRendererObj, Book.truncateStringPixels(this.displayPath, "...", 200, true), this.width / 2, 20, 14540253);
        if (!this.previewAuthor.equals("") || !this.previewTitle.equals("") || !this.previewPage.equals("")) {
            drawCenteredString(this.fontRendererObj, "Author: " + this.previewAuthor, this.width / 2, this.height - 50, 16777215);
            drawCenteredString(this.fontRendererObj, "Title: " + this.previewTitle, this.width / 2, this.height - 40, 16777215);
            drawCenteredString(this.fontRendererObj, "Page 1: " + this.previewPage, this.width / 2, this.height - 30, 16777215);
        }
    }

    protected void actionPerformed(GuiButton buttonPressed) {
        if (!buttonPressed.enabled)
            return;
        switch (buttonPressed.id) {
            case 0:
                if (this.tempBook != null) {
                    this.parentScreen.setBook(this.tempBook);
                    Minecraft.getMinecraft().displayGuiScreen(this.parentScreen);
                }
                break;
            case 1:
                Minecraft.getMinecraft().displayGuiScreen(this.parentScreen);
                break;
        }
        if (buttonPressed.id >= 100)
            this.fileHandler.currentPath = new File(buttonPressed.displayString);
    }

    class ScrollList extends GuiSlot {
        private static final int SLOT_HEIGHT = 12;

        public ScrollList() {
            super(GuiFileBrowser.this.mc, GuiFileBrowser.this.width, GuiFileBrowser.this.height, 32, GuiFileBrowser.this.height - 64, 12);
        }

        protected int getPaddedSize() {
            int scrollHeight = GuiFileBrowser.this.height - 96;
            // Use floating point division to avoid truncation when calculating
            // how many slots should be visible.
            int minSlots = (int) Math.ceil(scrollHeight / 12.0);
            if (GuiFileBrowser.this.listItems.size() >= minSlots)
                return GuiFileBrowser.this.listItems.size();
            return minSlots;
        }

        protected int getSize() {
            return getPaddedSize();
        }

        protected void elementClicked(int slotClicked, boolean doubleClicked, int clickXPos, int clickYPos) {
            setShowSelectionBox(true);
            if (doubleClicked) {
                if (slotClicked == 0) {
                    GuiFileBrowser.this.fileHandler.navigateUp();
                    GuiFileBrowser.this.slotSelected = -1;
                    setShowSelectionBox(false);
                    return;
                }
                if (slotClicked <= GuiFileBrowser.this.listItems.size()) {
                    File itemClicked = GuiFileBrowser.this.listItems.get(slotClicked - 1);
                    if (itemClicked.isDirectory()) {
                        GuiFileBrowser.this.fileHandler.currentPath = itemClicked;
                        GuiFileBrowser.this.slotSelected = -1;
                        setShowSelectionBox(false);
                        return;
                    }
                    if (GuiFileBrowser.this.tempBook != null) {
                        GuiFileBrowser.this.parentScreen.setBook(GuiFileBrowser.this.tempBook);
                        Minecraft.getMinecraft().displayGuiScreen(GuiFileBrowser.this.parentScreen);
                    }
                }
            } else if (slotClicked > 0 && slotClicked <= GuiFileBrowser.this.listItems.size()) {
                File selectedFile = GuiFileBrowser.this.listItems.get(slotClicked - 1);
                if (selectedFile.isFile() && !isSelected(slotClicked)) {
                    GuiFileBrowser.this.loadPreview(selectedFile);
                } else {
                    GuiFileBrowser.this.previewTitle = "";
                    GuiFileBrowser.this.previewAuthor = "";
                    GuiFileBrowser.this.previewPage = "";
                    GuiFileBrowser.this.tempBook = null;
                }
            } else {
                GuiFileBrowser.this.previewTitle = "";
                GuiFileBrowser.this.previewAuthor = "";
                GuiFileBrowser.this.previewPage = "";
                GuiFileBrowser.this.tempBook = null;
            }
            GuiFileBrowser.this.slotSelected = slotClicked;
        }

        protected boolean isSelected(int pos) {
            return (pos == GuiFileBrowser.this.slotSelected);
        }

        protected int getContentHeight() {
            return getPaddedSize() * 12;
        }

        protected void drawBackground() {
            GuiFileBrowser.this.drawDefaultBackground();
        }

        protected void drawSlot(int slotNum, int p_148126_2_, int p_148126_3_, int p_148126_4_, Tessellator p_148126_5_, int p_148126_6_, int p_148126_7_) {
            List<File> list = GuiFileBrowser.this.listItems;
            if (slotNum > list.size())
                return;
            String slotText = "";
            int color = 16777215;
            if (slotNum == 0) {
                slotText = "..";
                color = 65280;
            } else {
                slotText = Book.truncateStringPixels(((File) list.get(slotNum - 1)).getName(), "...", 200, false);
                if (((File) list.get(slotNum - 1)).isFile()) {
                    color = 16711680;
                } else {
                    color = 65280;
                }
            }
            GuiFileBrowser.this.drawString(GuiFileBrowser.this.fontRendererObj, slotText, p_148126_2_ + 2, p_148126_3_ + 1, color);
        }
    }
}
