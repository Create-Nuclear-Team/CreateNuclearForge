package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.CNTags.CNItemTags;
import net.nuclearteam.createnuclear.content.multiblock.VirtualReactorInputs;
import net.nuclearteam.createnuclear.content.multiblock.input.ReactorInputEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for reactor input positions (`ReactorInput`).
 *
 * Serializes positions as x/y/z triplets and provides utilities to
 * obtain valid `IItemHandler` instances present at those positions.
 */
public class ReactorInputManager extends AbstractReactorIOManager implements ReactorInputManagerI {
    private static final String NBT_KEY = "ReactorInput";

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
    public void read(CompoundTag compound) {
        positions.clear();
//        CreateNuclear.LOGGER.warn("ReactorInputManager::read start: {} {}", compound.contains(NBT_KEY), compound.getList(NBT_KEY, Tag.TAG_COMPOUND));
        if (!compound.contains(NBT_KEY)) return;
        ListTag list = compound.getList(NBT_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag tag = list.getCompound(i);
            positions.add(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
        }
    }

    /**
     * Retrieves all `IItemHandler` instances located at the input positions.
     * Returns an empty list when no handlers are found.
     */
    @Override
    public List<IItemHandler> getItemHandlers(Level level) {
        List<IItemHandler> handlers = new ArrayList<>();
        for (BlockPos p: new ArrayList<>(positions)) {
            if (level == null || !level.isLoaded(p)) continue;
            BlockEntity be = level.getBlockEntity(p);
            if (be == null) continue;
            LazyOptional<IItemHandler> cap = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
            cap.ifPresent(handlers::add);
        }

        return handlers;
    }

    @Override
    public VirtualReactorInputs getInventory(Level level) {
        List<IItemHandler> handlers = this.getItemHandlers(level);
        if (handlers.isEmpty()) return new VirtualReactorInputs();

        int totalFuel = 0;
        int totalCooler = 0;
        for (IItemHandler h : handlers) {
            int slots = h.getSlots();
            for (int s = 0; s < slots; s++) {
                ItemStack st = h.getStackInSlot(s);
                if (st.is(CNTags.CNItemTags.FUEL.tag)) totalFuel += st.getCount();
                else if (st.is(CNTags.CNItemTags.COOLER.tag)) totalCooler += st.getCount();
            }
        }

        return new VirtualReactorInputs(totalFuel, totalCooler);
    }

    @Override
    public boolean extractItems(Level level, int fuelNeeded, int coolerNeeded) {
        if (level == null) return false;
        List<IItemHandler> handlers = getItemHandlers(level);
        if (handlers.isEmpty()) return false;

        int fuelRemaining = fuelNeeded;
        int coolerRemaining = coolerNeeded;

        for (IItemHandler handler : handlers) {
            int slots = handler.getSlots();
            for (int s = 0; s < slots && (fuelRemaining > 0 || coolerRemaining > 0); s++) {
                ItemStack stack = handler.getStackInSlot(s);
                if (stack.isEmpty()) continue;
                if (fuelRemaining > 0 && stack.is(CNItemTags.FUEL.tag)) {
                    int toExtract = Math.min(fuelRemaining, stack.getCount());
                    handler.extractItem(s, toExtract, false);
                    fuelRemaining -= toExtract;
                } else if (coolerRemaining > 0 && stack.is(CNItemTags.COOLER.tag)) {
                    int toExtract = Math.min(coolerRemaining, stack.getCount());
                    handler.extractItem(s, toExtract, false);
                    coolerRemaining -= toExtract;
                }
            }
            if (fuelRemaining <= 0 && coolerRemaining <= 0) break;
        }

        return fuelRemaining <= 0 && coolerRemaining <= 0;
    }

    /**
     * Cleans up invalid positions (unloaded chunk, missing block entity,
     * or not a `Container`).
     */
    @Override
    public void clearInvalid(Level level) {
        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos p: positions) {
            if (level == null || !level.isLoaded(p)) {
                toRemove.add(p);
                continue;
            }

            BlockEntity be = level.getBlockEntity(p);
            if (be == null || !(be instanceof Container)) toRemove.add(p);
        }

        positions.removeAll(toRemove);
    }

    public List<BlockPos> getBlocksPosition(Level level) {
        List<BlockPos> positions = new ArrayList<>();

        for (BlockPos p : this.getBlocksPosition()) {
            if (level.getBlockEntity(p) instanceof ReactorInputEntity) positions.add(p);
        }
        return List.copyOf(positions);
    }
}
