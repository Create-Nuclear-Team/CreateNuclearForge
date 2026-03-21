package net.nuclearteam.createnuclear.content.multiblock.input.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory manager for ephemeral fluid locks on multiblock controllers.
 * This class provides simple concurrent lock semantics to ensure a controller
 * accepts only a single fluid type while a lock is held.
 */
public class FluidLockManager {
    private static final Map<BlockPos, Fluid> locks = new ConcurrentHashMap<>();

    public static boolean tryLock(BlockPos controllerPos, Fluid fluid) {
        if (fluid == null) return true;
        // Attempt to set the lock to the specified fluid if none exists
        return locks.compute(controllerPos, (k,v) -> v == null ? fluid : v) == fluid;
    }

    public static boolean canAccept(BlockPos controllerPos, FluidStack stack) {
        if (stack == null || stack.isEmpty()) return true;
        // True if no fluid locked or the locked fluid matches the stack
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
