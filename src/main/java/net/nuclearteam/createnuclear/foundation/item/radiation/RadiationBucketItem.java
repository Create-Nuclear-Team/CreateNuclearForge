package net.nuclearteam.createnuclear.foundation.item.radiation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.nuclearteam.createnuclear.api.radiation.IRadiationSource;

import java.util.function.Supplier;

public class RadiationBucketItem extends BucketItem implements IRadiationSource {
    private final double radiation;

    public RadiationBucketItem(Supplier<? extends Fluid> supplier, Properties builder, double radiation) {
        super(supplier, builder);
        this.radiation = radiation;
    }

    @Override
    public double getRadiation(ItemStack stack, Player player) {
        return this.radiation;
    }
}
