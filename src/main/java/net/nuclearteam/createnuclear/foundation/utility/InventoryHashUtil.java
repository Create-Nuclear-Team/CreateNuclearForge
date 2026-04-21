package net.nuclearteam.createnuclear.foundation.utility;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InventoryHashUtil {
    public static long compute(Player player) {
        long hash = 1;

        for (ItemStack stack : player.getInventory().items) {
            hash = 31 * hash + stackHash(stack);
        }

        for (ItemStack stack : player.getInventory().offhand) {
            hash = 31 * hash + stackHash(stack);
        }

        return hash;
    }

    private static long stackHash(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        long h = Item.getId(stack.getItem());
        h = 31 * h + stack.getCount();

        if (stack.hasTag()) {
            h = 31 * h + stack.getTag().hashCode();
        }

        return h;
    }
}
