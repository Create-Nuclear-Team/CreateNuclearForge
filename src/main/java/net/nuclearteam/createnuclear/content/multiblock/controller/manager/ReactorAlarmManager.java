package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.content.multiblock.alarm.ReactorAlarmEntity;

import java.util.ArrayList;
import java.util.List;

public class ReactorAlarmManager extends AbstractReactorIOManager implements ReactorAlarmManagerI {
    private static final String NBT_KEY = "ReactorAlarms";

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
        if (!compound.contains(NBT_KEY)) return;
        ListTag list = compound.getList(NBT_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag tag = list.getCompound(i);
            positions.add(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
        }
    }

    @Override
    public void clearInvalid(Level level) {
        if (level == null) return;
        List<BlockPos> toRemove = new ArrayList<>();

        for (BlockPos p : positions) {
            if (!level.isLoaded(p)) continue; // On ne supprime pas si le chunk est juste déchargé

            BlockEntity be = level.getBlockEntity(p);
            // Si le bloc n'existe plus ou n'est plus une alarme, on marque pour suppression
            if (be == null || !(be instanceof ReactorAlarmEntity)) {
                toRemove.add(p);
            }
        }
        positions.removeAll(toRemove);
    }

    @Override
    public List<BlockPos> getBlocksPosition(Level level) {
        if (level == null) return List.of();

        List<BlockPos> validPositions = new ArrayList<>();
        for (BlockPos p : this.positions) {
            // On vérifie que l'entité est bien chargée et du bon type
            if (level.isLoaded(p) && level.getBlockEntity(p) instanceof ReactorAlarmEntity) {
                validPositions.add(p);
            }
        }
        return List.copyOf(validPositions);
    }
}