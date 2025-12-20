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

public class ReactorInputManager extends AbstractReactorIOManager {
    private final List<BlockPos> inputPositions = new ArrayList<>();
    private static final String NBT_KEY = "ReactorInput";

    @Override
    public boolean addBlock(BlockPos pos) {
        if (pos == null) return false;
        if (!this.contains(pos)) {
            inputPositions.add(pos);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeBlock(BlockPos pos) {
        return inputPositions.remove(pos);
    }

    @Override
    public boolean contains(BlockPos pos) {
        return inputPositions.contains(pos);
    }

    @Override
    public int size() {
        return inputPositions.size();
    }

    @Override
    public List<BlockPos> getBlocksPosition() {
        return List.copyOf(inputPositions);
    }

    @Override
    public void write(CompoundTag compound) {
        ListTag list = new ListTag();
        CreateNuclear.LOGGER.warn("ReactorInputManager::write start: {}", inputPositions.isEmpty());
        for (BlockPos pos : inputPositions) {
            CreateNuclear.LOGGER.warn("ReactorInputManager::write pos: {}", pos);
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
        inputPositions.clear();
        CreateNuclear.LOGGER.warn("ReactorInputManager::read start: {} {}", compound.contains(NBT_KEY), compound.getList(NBT_KEY, Tag.TAG_COMPOUND));
        if (!compound.contains(NBT_KEY)) return;
        ListTag list = compound.getList(NBT_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag tag = list.getCompound(i);
            inputPositions.add(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
        }
    }

    @Override
    public <T> List<T> resolveBlock(Level level, Function<BlockPos, T> resolver) {
        List<T> result = new ArrayList<>();
        for (BlockPos p: new ArrayList<>(inputPositions)) {
            T r = resolver.apply(p);
            if (r != null) result.add(r);
        }
        return result;
    }

    public List<IItemHandler> getItemHandlers(Level level) {
        List<IItemHandler> handlers = new ArrayList<>();
        for (BlockPos p: new ArrayList<>(inputPositions)) {
            if (level == null || !level.isLoaded(p)) continue;
            BlockEntity be = level.getBlockEntity(p);
            if (be == null) continue;
            LazyOptional<IItemHandler> cap = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
            cap.ifPresent(handlers::add);
        }

        return handlers;
    }

    @Override
    public void clearInvalid(Level level) {
        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos p: inputPositions) {
            if (level == null || !level.isLoaded(p)) {
                toRemove.add(p);
                continue;
            }

            BlockEntity be = level.getBlockEntity(p);
            if (be == null || !(be instanceof Container)) toRemove.add(p);
        }

        inputPositions.removeAll(toRemove);
    }
}
