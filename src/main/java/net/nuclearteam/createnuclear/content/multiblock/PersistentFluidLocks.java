package net.nuclearteam.createnuclear.content.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentFluidLocks extends SavedData {
    private final Map<BlockPos, Fluid> locks = new ConcurrentHashMap<>();
    private static final String DATA_NAME = "createnuclear_fluid_locks";

    public static PersistentFluidLocks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(nbt -> {
            PersistentFluidLocks d = new PersistentFluidLocks();
            d.readFrom(nbt);

            return d;
        }, PersistentFluidLocks::new, DATA_NAME);
    }

   public boolean tryLock(BlockPos pos, Fluid fluid) {
        if (fluid == null) return true;
        boolean ok = locks.compute(pos, (k,v) -> v == null ? fluid : v) == fluid;
        if (ok) setDirty();
        return ok;
   }

    public boolean canAccept(BlockPos pos, Fluid fluid) {
        if (fluid == null) return true;
        Fluid locked = locks.get(pos);
        return locked == null || locked == fluid;
    }

    public void clearLock(BlockPos pos) {
        if (locks.remove(pos) != null) setDirty();
    }

    public void readFrom(CompoundTag nbt) {
        locks.clear();
        ListTag list = nbt.getList("locks", 10);
        list.forEach(tag -> {
            CompoundTag e = (CompoundTag) tag;
            BlockPos pos = new BlockPos(e.getInt("x"), e.getInt("y"), e.getInt("z"));
            Fluid f = ForgeRegistries.FLUIDS.getValue(net.minecraft.resources.ResourceLocation.tryParse(e.getString("fluid")));
            if (f != null) locks.put(pos, f);
        });
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag list = new ListTag();
        locks.forEach((pos, fluid) -> {
            CompoundTag e = new CompoundTag();
            e.putInt("x", pos.getX());
            e.putInt("y", pos.getY());
            e.putInt("z", pos.getZ());
            e.putString("fluid", ForgeRegistries.FLUIDS.getKey(fluid).toString());
            list.add(e);
        });
        nbt.put("locks", list);
        return nbt;
    }
}
