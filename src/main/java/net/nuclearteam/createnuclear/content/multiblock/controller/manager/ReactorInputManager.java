package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
        CreateNuclear.LOGGER.warn("ReactorInputManager::read start: {} {}", compound.contains(NBT_KEY), compound.getList(NBT_KEY, Tag.TAG_COMPOUND));
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
}
