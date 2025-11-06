package kamkeel.bookeditor.util;

import kamkeel.bookeditor.book.format.FormattingOptions;
import kamkeel.bookeditor.book.format.FormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public final class BookUtil {
    private BookUtil() {
    }

    public static ItemStack safeGetHeldItem(Minecraft minecraft) {
        if (minecraft == null || minecraft.thePlayer == null) {
            return null;
        }
        return minecraft.thePlayer.getHeldItem();
    }

    public static boolean isWritableBook(ItemStack stack) {
        return stack != null && stack.getItem() != null && stack.getItem().equals(Items.writable_book);
    }

    public static boolean isAnyBook(ItemStack stack) {
        return stack != null && stack.getItem() != null
            && (stack.getItem().equals(Items.writable_book) || stack.getItem().equals(Items.written_book));
    }

    public static String sanitizeBookText(String text) {
        return FormattingUtil.sanitizeFormatting(text, FormattingOptions.defaults());
    }
}
