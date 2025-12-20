package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractReactorIOManager {
    abstract public boolean contains(BlockPos pos);
    abstract public int size();
    abstract public void read(CompoundTag compound);
    abstract public void write(CompoundTag compound);

    abstract public boolean addBlock(BlockPos pos);
    abstract public boolean removeBlock(BlockPos pos);
    abstract public List<BlockPos> getBlocksPosition();

    abstract public void clearInvalid(Level level);
    abstract public <T> List<T> resolveBlock(Level level, Function<BlockPos, T> resolver);

}
