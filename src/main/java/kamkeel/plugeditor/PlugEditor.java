package kamkeel.plugeditor;

/**
 * Entry point Forge mod that installs the plug editor functionality. This class
 * listens to game ticks to swap out the regular book GUI with the editable
 * version and manages clipboard state for copying and pasting pages.
 */

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

import kamkeel.plugeditor.book.Book;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import kamkeel.plugeditor.gui.GuiPlugBook;

import static kamkeel.plugeditor.PlugEditor.*;

@Mod(modid = MODID, version = VERSION, name = NAME)
public class PlugEditor {
    public static final String MODID = "plugeditor";
    public static final String VERSION = "1.1";
    public static final String NAME = "Plug Editor";

    private Minecraft mc = Minecraft.getMinecraft();

    private int connectWait = 10;

    private boolean connected = false;

    private int firstGuiOpenWait = 20;

    private boolean firstGuiOpen = false;

    private Book bookClipboard;

    private List<String> pageClipboard;

    public PlugEditor() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        this.bookClipboard = new Book();
        this.pageClipboard = new ArrayList<String>();
    }

    @SubscribeEvent
    public void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (this.mc.currentScreen instanceof net.minecraft.client.gui.GuiScreenBook)
                this.mc.displayGuiScreen((GuiScreen) new GuiPlugBook(this.bookClipboard, this.pageClipboard));
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
