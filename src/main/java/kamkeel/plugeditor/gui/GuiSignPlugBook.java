package kamkeel.plugeditor.gui;

/**
 * GUI shown when the user is finalising a book. Allows entering the title and
 * author before the book is locked via signing.
 */

import kamkeel.plugeditor.book.Book;
import kamkeel.plugeditor.constants.Buttons;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiSignPlugBook extends GuiScreen {
    private int bookImageWidth = 192;
    private int bookImageHeight = 192;
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
    private int updateCount = 0;
    private boolean titleSelected = true;

    // The sign GUI now uses ButtonIds for its buttons.
    private GuiButton btnFinalise;
    private Book book;
    private GuiPlugBook parentScreen;

    public GuiSignPlugBook(Book _book, GuiPlugBook _parentScreen) {
        this.book = _book;
        this.parentScreen = _parentScreen;
    }

    @Override
    public void initGui() {
        this.buttonList.add(this.btnFinalise = new GuiButton(Buttons.BTN_SIGN_FINALISE, this.width / 2 - 100, 4 + this.bookImageHeight, 98, 20, "Finalise"));
        this.buttonList.add(new GuiButton(Buttons.BTN_SIGN_CANCEL, this.width / 2 + 2, 4 + this.bookImageHeight, 98, 20, "Cancel"));
        updateButtons();
    }

    @Override
    protected void actionPerformed(GuiButton buttonPressed) {
        if (!buttonPressed.enabled)
            return;
        switch (buttonPressed.id) {
            case Buttons.BTN_SIGN_CANCEL:
                goBackToParentScreen();
                break;
            case Buttons.BTN_SIGN_FINALISE:
                this.book.sendBookToServer(true);
                this.mc.displayGuiScreen(null);
                break;
        }
    }

    @Override
    protected void mouseClicked(int posX, int posY, int button) {
        int bookLeftSide = (this.width - this.bookImageWidth) / 2;
        int bookTextLeft = bookLeftSide + 36;
        if (posX >= bookTextLeft && posX < bookTextLeft + 116) {
            if (posY >= 45 && posY <= 54) {
                this.titleSelected = true;
            } else if (posY >= 69 && posY <= 78) {
                this.titleSelected = false;
            }
        }
        super.mouseClicked(posX, posY, button);
    }

    private void goBackToParentScreen() {
        this.mc.displayGuiScreen(this.parentScreen);
    }

    @Override
    protected void keyTyped(char character, int keycode) {
        switch (keycode) {
            case 1:
                goBackToParentScreen();
                return;
            case 15:
            case 28:
            case 156:
                this.titleSelected = !this.titleSelected;
                return;
            case 14:
                if (this.titleSelected) {
                    if (this.book.title.length() > 0)
                        this.book.title = this.book.title.substring(0, this.book.title.length() - 1);
                } else if (this.book.author.length() > 0) {
                    this.book.author = this.book.author.substring(0, this.book.author.length() - 1);
                }
                return;
        }
        if (ChatAllowedCharacters.isAllowedCharacter(character)) {
            if (this.titleSelected) {
                if (this.book.title.length() < 16)
                    this.book.title += character;
            } else {
                if (this.book.author.length() < 16)
                    this.book.author += character;
            }
        }
        updateButtons();
    }

    private void updateButtons() {
        this.btnFinalise.enabled = !this.book.title.isEmpty() && !this.book.author.isEmpty();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.updateCount++;
    }

    private void drawCenteredBookString(String str, int top, int colour) {
        int bookLeftSide = (this.width - this.bookImageWidth) / 2;
        int strWidth = this.fontRendererObj.getStringWidth(str);
        this.fontRendererObj.drawString(str, bookLeftSide + 36 + (116 - strWidth) / 2, top, colour);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookGuiTextures);
        int bookLeftSide = (this.width - this.bookImageWidth) / 2;
        byte topOffset = 2;
        drawTexturedModalRect(bookLeftSide, topOffset, 0, 0, this.bookImageWidth, this.bookImageHeight);
        String cursor = (this.updateCount / 10 % 2 == 0) ? EnumChatFormatting.BLACK + "_" : EnumChatFormatting.GRAY + "_";
        String titleLine = this.book.title;
        String authorLine = this.book.author;
        if (this.titleSelected) {
            titleLine += cursor;
        } else {
            authorLine += cursor;
        }
        drawCenteredBookString("\u00a7lTitle:\u00a7r", 34, 0);
        drawCenteredBookString(titleLine, 46, 0);
        drawCenteredBookString("\u00a7lAuthor:\u00a7r", 58, 0);
        drawCenteredBookString(authorLine, 70, 0);
        String info = "Press tab to switch between the title and author fields.\n\nNote! When you sign the book, it will no longer be editable.";
        this.fontRendererObj.drawSplitString(info, bookLeftSide + 36, topOffset + 90, 116, 0);
        super.drawScreen(par1, par2, par3);
    }
}