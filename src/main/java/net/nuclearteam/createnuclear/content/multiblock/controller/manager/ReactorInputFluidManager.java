package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.VirtualReactorInputFluid;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputEntity;

import java.util.ArrayList;
import java.util.List;

public class ReactorInputFluidManager extends AbstractReactorIOManager implements ReactorInputFluidManagerI {
    private static final String NBT_KEY = "ReactorInputFluid";

    @Override
    public void read(CompoundTag compound) {
        positions.clear();
        if (!compound.contains(NBT_KEY)) return;
        ListTag list = compound.getList(NBT_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag tag = list.getCompound(i);
            positions.add(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
        }
    }

    @Override
    public void write(CompoundTag compound) {
        ListTag list = new ListTag();
        for (BlockPos pos : positions) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            list.add(tag);
        }
        compound.put(NBT_KEY, list);
    }

    @Override
    public void clearInvalid(Level level) {
        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos p: positions) {
            if (level == null || !level.isLoaded(p)) {
                toRemove.add(p);
                continue;
            }

            BlockEntity be = level.getBlockEntity(p);
            if (be == null) {
                toRemove.add(p);
                continue;
            }

            LazyOptional<IFluidHandler> cap = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
            if (cap == null || !cap.isPresent()) toRemove.add(p);
        }

        positions.removeAll(toRemove);
    }

    @Override
    public List<BlockPos> getBlocksPosition(Level level) {
        List<BlockPos> positions = new ArrayList<>();

        for (BlockPos p : this.getBlocksPosition()) {
            CreateNuclear.LOGGER.warn("getBlocksPosition: {} {}", level.getBlockEntity(p), level.getBlockEntity(p) instanceof ReactorFluidInputEntity);
            if (level.getBlockEntity(p) instanceof ReactorFluidInputEntity) positions.add(p);
        }
        return List.copyOf(positions);
    }

    @Override
    public List<IFluidHandler> getFuildHandlers(Level level) {
        List<IFluidHandler> handlers = new ArrayList<>();
        for (BlockPos p : new ArrayList<>(positions)) {
            if (level == null || !level.isLoaded(p)) continue;
            BlockEntity be = level.getBlockEntity(p);
            if (be == null) continue;
            LazyOptional<IFluidHandler> cap = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
            cap.ifPresent(handlers::add);
        }

        return handlers;
    }

    @Override
    public VirtualReactorInputFluid getInventory(Level level) {
        VirtualReactorInputFluid virtualReactorInputFluid = new VirtualReactorInputFluid();
        List<IFluidHandler> handlers = this.getFuildHandlers(level);
        if (handlers.isEmpty()) return new VirtualReactorInputFluid();

        for (IFluidHandler h : handlers) {
            int tank = h.getTanks();
            virtualReactorInputFluid.addFluid(h.getFluidInTank(tank));
        }

        return virtualReactorInputFluid;
    }

    @Override
    public boolean extractFluids(Level level, int fluidNeeded) {
        if (level == null) return false;
        List<IFluidHandler> handlers = getFuildHandlers(level);
        if (handlers.isEmpty()) return false;

        int fluidRemaining = fluidNeeded;

        for (IFluidHandler handler : handlers) {
            int tank = handler.getTanks();
            FluidStack stack = handler.getFluidInTank(tank);
            if (stack.isEmpty()) continue;
            if (fluidRemaining > 0) {
                int toExtract = Math.min(fluidRemaining, stack.getAmount());
                handler.drain(toExtract, FluidAction.EXECUTE);
                fluidRemaining -= toExtract;
            }

            if (fluidRemaining <= 0) break;
        }

        return fluidRemaining <= 0;
    }
}
