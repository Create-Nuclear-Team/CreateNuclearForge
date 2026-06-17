package net.nuclearteam.createnuclear.content.radiation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.nuclearteam.createnuclear.api.radiation.IRadiationSource;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class RadiationBucketItem extends BucketItem implements IRadiationSource {
    private final double radiation;

    public RadiationBucketItem(Supplier<? extends Fluid> supplier, Properties builder, double radiation) {
        super(supplier, builder);
        this.radiation = radiation;
    }

    /**
     * Forge's {@link BucketItem#initCapabilities} only provides the {@link FluidBucketWrapper}
     * fluid-handler capability when {@code getClass() == BucketItem.class}. Because this is a
     * subclass, it would otherwise return no fluid capability — making the bucket impossible to
     * empty into fluid handlers (e.g. Create fluid tanks). Re-declare it here to restore that.
     */
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidBucketWrapper(stack);
    }

    @Override
    public double getRadiation(ItemStack stack, Player player) {
        return this.radiation;
    }
}
