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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.MultiblockHelpers;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReactorFluidInputEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    /** Capacité par défaut tant que l'input n'est rattaché à aucun réacteur assemblé. */
    public static final int DEFAULT_CAPACITY = 16000;

    private final FluidTank internalTank;
    private LazyOptional<IFluidHandler> capability;
    private LerpedFloat fluidLevel;

    public ReactorFluidInputEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        internalTank = new SmartFluidTank(DEFAULT_CAPACITY, this::onTankContentsChanged);
        capability = LazyOptional.of(() -> internalTank);

        refreshCapability();
    }

    /**
     * Capacité du tank en fonction de la taille du réacteur (tier).
     * 5x5 -> tier 1, 7x7 -> tier 2, 9x9 -> tier 3.
     */
    public static int getCapacityForReactorSize(int reactorSize) {
        return switch (reactorSize) {
            case 5 -> 144000;
            case 7 -> 448000;
            case 9 -> 848000;
            default -> DEFAULT_CAPACITY;
        };
    }

    /** Applique la capacité correspondant à la taille du réacteur à laquelle cet input appartient. */
    public void applyReactorTierCapacity(int reactorSize) {
        int capacity = getCapacityForReactorSize(reactorSize);
        if (internalTank.getCapacity() == capacity) return;
        internalTank.setCapacity(capacity);
        if (level != null && !level.isClientSide) {
            setChanged();
            sendData();
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        CompoundTag tankTag = internalTank.writeToNBT(new CompoundTag());
        tag.put("tank", tankTag);
        tag.putInt("capacity", internalTank.getCapacity());
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (tag.contains("capacity"))
            internalTank.setCapacity(tag.getInt("capacity"));
        internalTank.readFromNBT(tag.getCompound("tank"));

        if (tag.contains("ForceFluidLevel") || fluidLevel == null)
            fluidLevel = LerpedFloat.linear()
                    .startWithValue(getFillState());
    }

    public float getFillState() {
        return (float) internalTank.getFluidAmount() / internalTank.getCapacity();
    }

    protected void onTankContentsChanged(FluidStack contents) {
        // Avoid accessing level during deserialization when the block entity isn't attached yet
        if (this.level == null) {
            if (fluidLevel == null)
                fluidLevel = LerpedFloat.linear()
                        .startWithValue(getFillState());
            return;
        }

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

        if (!level.isClientSide && internalTank.getFluidAmount() == 0) {
            ReactorControllerBlockEntity controller = MultiblockHelpers.getControllerForPart(level, worldPosition);
            if (controller != null) controller.clearLockIfAllInputsEmpty();
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
        if (cap == ForgeCapabilities.FLUID_HANDLER) return capability.cast();

        return super.getCapability(cap, side);
    }

    private void refreshCapability() {
        LazyOptional<IFluidHandler> oldCap = capability;
        capability = LazyOptional.of(FilteredFluidHandler::new);
        oldCap.invalidate();
    }

    private class FilteredFluidHandler implements IFluidHandler {
        private final IFluidHandler delegate = internalTank;

        @Override
        public int getTanks() {
            return delegate.getTanks();
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return delegate.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return delegate.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return delegate.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) return 0;
            ReactorControllerBlockEntity controller = MultiblockHelpers.getControllerForPart(level, worldPosition);
            BlockPos controllerPos = controller != null ? controller.getBlockPos() : null;

            if (controllerPos != null && level instanceof ServerLevel serverLevel) {
                PersistentFluidLocks lock = PersistentFluidLocks.get(serverLevel);
                if (!lock.canAccept(controllerPos, resource.getFluid())) return 0;
            } else if (controllerPos != null) {
                if (!FluidLockManager.canAccept(controllerPos, resource)) return 0;
            }

            int filled = delegate.fill(resource, action);
            if (filled > 0 && action.execute() && controllerPos != null ){
                if (level instanceof ServerLevel serverLevel) {
                    PersistentFluidLocks.get(serverLevel).tryLock(controllerPos, resource.getFluid());
                } else {
                    FluidLockManager.tryLock(controllerPos, resource.getFluid());
                }
            }
            return filled;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack drained = delegate.drain(resource, action);
            if (!drained.isEmpty() && action.execute()) {
                ReactorControllerBlockEntity controller = MultiblockHelpers.getControllerForPart(level, worldPosition);
                if (controller != null) {
                    BlockPos controllerPos = controller.getBlockPos();
                    if (delegate.getFluidInTank(0).isEmpty()) {
                        if (level instanceof ServerLevel serverLevel) {
                            PersistentFluidLocks.get(serverLevel).clearLock(controllerPos);
                        } else {
                            FluidLockManager.clearLock(controllerPos);
                        }
                    }
                }
            }
            return drained;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack drained = delegate.drain(maxDrain, action);
            if (!drained.isEmpty() && action.execute()) {
                ReactorControllerBlockEntity controller = MultiblockHelpers.getControllerForPart(level, worldPosition);
                if (controller != null) {
                    BlockPos controllerPos = controller.getBlockPos();
                    if (delegate.getFluidInTank(0).isEmpty()) {
                        if (level instanceof ServerLevel serverLevel) {
                            PersistentFluidLocks.get(serverLevel).clearLock(controllerPos);
                        } else {
                            FluidLockManager.clearLock(controllerPos);
                        }
                    }
                }
            }
            return drained;
        }
    }
}
