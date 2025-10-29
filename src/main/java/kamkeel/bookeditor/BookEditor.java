package kamkeel.bookeditor;

/**
 * Entry point Forge mod that installs the book editor functionality. This class
 * listens to game ticks to swap out the regular book GUI with the editable
 * version and manages clipboard state for copying and pasting pages.
 */

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import kamkeel.bookeditor.book.Book;
import kamkeel.bookeditor.gui.GuiBookEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

import static kamkeel.bookeditor.BookEditor.*;

@Mod(modid = MODID, version = VERSION, name = NAME)
public class BookEditor {
    public static final String MODID = "bookeditor";
    public static final String VERSION = "1.0";
    public static final String NAME = "Book Editor";

    private Minecraft mc = Minecraft.getMinecraft();

    private int connectWait = 10;

    private boolean connected = false;

    private int firstGuiOpenWait = 20;

    private boolean firstGuiOpen = false;

    private Book bookClipboard;

    private List<String> pageClipboard;

    public BookEditor() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        this.bookClipboard = new Book();
        this.pageClipboard = new ArrayList<String>();
    }

    @SubscribeEvent
    public void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (this.mc.currentScreen instanceof net.minecraft.client.gui.GuiScreenBook)
                this.mc.displayGuiScreen((GuiScreen) new GuiBookEditor(this.bookClipboard, this.pageClipboard));
            if (!this.firstGuiOpen &&
                this.firstGuiOpenWait-- <= 0)
                this.firstGuiOpen = true;
        }
    }

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END)
            if (!this.connected &&
                this.connectWait-- <= 0)
                this.connected = true;
    }
}
