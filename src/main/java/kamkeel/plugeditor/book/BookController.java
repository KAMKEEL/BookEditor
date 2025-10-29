package kamkeel.plugeditor.book;

import cpw.mods.fml.common.Loader;
import kamkeel.plugeditor.util.BookTextFormatter;
import kamkeel.plugeditor.util.BookUtils;
import kamkeel.plugeditor.util.HexTextCompat;

/**
 * Central controller for book-related runtime state. This tracks whether
 * optional integrations such as HexText are available so the rest of the editor
 * can react accordingly without repeatedly querying Forge.
 */
public final class BookController {
    private static final BookController INSTANCE = new BookController();

    private boolean hexTextLoaded;
    private BookTextFormatter textFormatter = BookUtils.INSTANCE;

    private BookController() {
    }

    public static BookController getInstance() {
        return INSTANCE;
    }

    public void preInit() {
        this.hexTextLoaded = Loader.isModLoaded("hextext");
        if (this.hexTextLoaded) {
            try {
                this.textFormatter = HexTextCompat.INSTANCE;
            } catch (Throwable throwable) {
                this.hexTextLoaded = false;
                this.textFormatter = BookUtils.INSTANCE;
                System.out.println("[BookEditor] HexText unavailable, falling back to legacy formatter: " + throwable.getMessage());
            }
        } else {
            this.textFormatter = BookUtils.INSTANCE;
        }
    }

    public boolean isHexTextLoaded() {
        return this.hexTextLoaded;
    }

    public BookTextFormatter getTextFormatter() {
        return this.textFormatter;
    }
}
