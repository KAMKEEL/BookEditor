package kamkeel.plugeditor.util;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * General helper methods for safely accessing book related item stacks.
 */
public final class MinecraftBookUtil {
    private MinecraftBookUtil() {
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
}
