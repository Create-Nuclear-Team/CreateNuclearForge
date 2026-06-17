package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for a reactor's outputs (`ReactorOutput`).
 * Handles serialization of output positions.
 */
public class ReactorOutputManager extends AbstractReactorIOManager implements ReactorOutputManagerI {
    private static final String NBT_KEY = "ReactorOutputs";

    @Override
    public void write(CompoundTag compound) {
        ListTag list = new ListTag();
        for (BlockPos p : positions) {
            CompoundTag t = new CompoundTag();
            t.putLong("p", p.asLong());
            list.add(t);
        }
        compound.put(NBT_KEY, list);
    }

    @Override
    public void read(CompoundTag compound) {
        positions.clear();
        if (!compound.contains(NBT_KEY)) return;
        ListTag list = compound.getList(NBT_KEY, 10);
        for (int i = 0; i < list.size(); i++) {
            BlockPos p = BlockPos.of(list.getCompound(i).getLong("p"));
            positions.add(p);
        }
    }

    @Override
    public void clearInvalid(Level level) {
        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos p : positions) {
            if (level == null || !level.isLoaded(p)) {
                toRemove.add(p);
                continue;
            }
            BlockEntity be = level.getBlockEntity(p);
            if (!(be instanceof ReactorOutputEntity)) toRemove.add(p);
        }
        positions.removeAll(toRemove);
    }

    @Override
    public List<BlockPos> getBlocksPosition(Level level) {
        List<BlockPos> positions = new ArrayList<>();

        for (BlockPos p : this.getBlocksPosition()) {
            if (level.getBlockEntity(p) instanceof ReactorOutputEntity) positions.add(p);
        }
        return List.copyOf(positions);
    }
}
