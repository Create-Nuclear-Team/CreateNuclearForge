package net.nuclearteam.createnuclear.api.radiation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IRadiationSource {
    double getRadiation(ItemStack stack, Player player);
}
