package net.nuclearteam.createnuclear.content.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FluidLockManager {
    private static final Map<BlockPos, Fluid> locks = new ConcurrentHashMap<>();

    public static boolean tryLock(BlockPos controllerPos, Fluid fluid) {
        if (fluid == null) return true;
        return locks.compute(controllerPos, (k,v) -> v == null ? fluid : v) == fluid;
    }

    public static boolean canAccept(BlockPos controllerPos, FluidStack stack) {
        if (stack == null || stack.isEmpty()) return true;
        Fluid locked = locks.get(controllerPos);
        return locked == null || locked == stack.getFluid();
    }

    public static void clearLock(BlockPos controller) {
        locks.remove(controller);
    }

    public static Fluid getLockedFluid(BlockPos controller) {
        return locks.get(controller);
    }
}
