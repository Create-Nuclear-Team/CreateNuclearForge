package net.nuclearteam.createnuclear.content.multiblock.input.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;

public record VirtualReactorInputFluid(Map<ResourceLocation, Long> fluids) {

    public VirtualReactorInputFluid() {
        this(new HashMap<>());
    }

    public void addFluid(@NotNull FluidStack stack) {
        if (stack.isEmpty() || stack.getAmount() <= 0) return;
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
        if (id == null) return;
        fluids.merge(id, (long) stack.getAmount(), Long::sum);
    }

    /**
     * Remove up to amount from the given fluid id and return a FluidStack representing what was removed.
     */
    public FluidStack removeFluid(@NotNull ResourceLocation fluidId, long amount) {
        if (amount <= 0 || fluidId == null) return FluidStack.EMPTY;
        long current = fluids.getOrDefault(fluidId, 0L);
        long removed = Math.min(current, amount);
        if (removed == 0) return FluidStack.EMPTY;
        long remaining = current - removed;
        if (remaining == 0) fluids.remove(fluidId);
        else fluids.put(fluidId, remaining);

        int removedInt = (int) Math.min(removed, Integer.MAX_VALUE);
        return new FluidStack(ForgeRegistries.FLUIDS.getValue(fluidId), removedInt);
    }

    public long getAmount(@NotNull ResourceLocation fluidId) {
        if (fluidId == null) return 0L;
        return fluids.getOrDefault(fluidId, 0L);
    }

    public static List<BigFluidStack> toBigList(Map<ResourceLocation, Long> map) {
        List<BigFluidStack> list = new ArrayList<>();
        for (Entry<ResourceLocation, Long> e : map.entrySet()) {
            ResourceLocation id = e.getKey();
            long total = e.getValue();
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
            if (fluid == null || total <= 0) continue;
            int amount = (int) Math.min(total, BigFluidStack.INF);
            list.add(new BigFluidStack(new FluidStack(fluid, amount), amount));
        }
        return list;
    }

    @Override
    public @NotNull String toString() {
        return "VirtualReactorInputFluid" + fluids.toString();
    }
}
