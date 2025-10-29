package kamkeel.bookeditor.util;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * Utility helpers when interacting with the player's held book item.
 * This class replaces the Angelica specific helper with a neutral,
 * game-focused implementation that works for vanilla Minecraft books.
 */
public final class BookItemUtil {
    private BookItemUtil() {
    }

    /**
     * Returns the item stack currently held by the player or {@code null}
     * if the Minecraft instance/player/held item is not available.
     */
    public static ItemStack safeGetHeldItem(Minecraft minecraft) {
        if (minecraft == null || minecraft.thePlayer == null) {
            return null;
        }
        return minecraft.thePlayer.getHeldItem();
    }

    /**
     * Checks if the provided stack represents a writable book.
     */
    public static boolean isWritableBook(ItemStack stack) {
        return stack != null && stack.getItem() != null && stack.getItem().equals(Items.writable_book);
    }

    /**
     * Checks if the provided stack represents either a writable or written book.
     */
    public static boolean isAnyBook(ItemStack stack) {
        return stack != null && stack.getItem() != null
            && (stack.getItem().equals(Items.writable_book) || stack.getItem().equals(Items.written_book));
    }
}
