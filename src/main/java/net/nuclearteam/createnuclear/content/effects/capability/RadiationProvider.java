package net.nuclearteam.createnuclear.content.effects.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RadiationProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<IRadiationCapability> CAP = CapabilityManager.get(new CapabilityToken<>() {});
    private final IRadiationCapability instance = new RadiationCapability();
    private final LazyOptional<IRadiationCapability> optional = LazyOptional.of(() -> instance);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAP ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("radiation", instance.getRadiation());
        tag.putLong("hash", instance.getInventoryHash());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.setRadiation(nbt.getDouble("radiation"));
        instance.setInventoryHash(nbt.getLong("hash"));
    }
}
