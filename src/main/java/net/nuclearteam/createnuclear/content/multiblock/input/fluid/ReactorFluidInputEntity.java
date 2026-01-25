package net.nuclearteam.createnuclear.content.multiblock.input.fluid;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReactorFluidInputEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private final SmartFluidTank internalTank;
    private LazyOptional<IFluidHandler> capability;
    private LerpedFloat fluidLevel;


    public ReactorFluidInputEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        internalTank = new SmartFluidTank(16000, this::onTankContentsChanged);
        capability = LazyOptional.of(() -> internalTank);

        refreshCapability();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        tag.put("tank", internalTank.writeToNBT(new CompoundTag()));
        super.write(tag, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        internalTank.readFromNBT(tag.getCompound("tank"));
        super.read(tag, clientPacket);

        if (tag.contains("ForceFluidLevel") || fluidLevel == null)
            fluidLevel = LerpedFloat.linear()
                    .startWithValue(getFillState());
    }

    public float getFillState() {
        return (float) internalTank.getFluidAmount() / internalTank.getCapacity();
    }

    protected void onTankContentsChanged(FluidStack contents) {
        if (!level.isClientSide) {
            setChanged();
            sendData();
        }

        if (isVirtual()) {
            if (fluidLevel == null)
                fluidLevel = LerpedFloat.linear()
                        .startWithValue(getFillState());
            fluidLevel.chase(getFillState(), .5f, LerpedFloat.Chaser.EXP);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        return containedFluidTooltip(tooltip, isPlayerSneaking,
                this.getCapability(ForgeCapabilities.FLUID_HANDLER));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        capability.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (!capability.isPresent()) refreshCapability();
        if (cap == ForgeCapabilities.FLUID_HANDLER) return  capability.cast();

        return super.getCapability(cap, side);
    }

    private void refreshCapability() {
        LazyOptional<IFluidHandler> oldCap = capability;
        capability = LazyOptional.of(() -> internalTank);
        oldCap.invalidate();
    }
}
