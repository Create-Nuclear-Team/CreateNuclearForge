package net.nuclearteam.createnuclear.content.multiblock.controller.service;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;
import net.nuclearteam.createnuclear.content.multiblock.controller.display.ReactorDisplayState;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.ReactorInputFluidManagerI;

/**
 * Centralizes the reactor's heat calculation logic and the decision of
 * whether it has enough fuel to run.
 */
public interface IReactorHeatUpdateCoordinator {

    /**
     * Determines whether the reactor has enough fuel (FUEL-type rods) to start,
     * on top of being assembled and having a fluid input.
     *
     * @return {@code true} if every fuel rod required by the pattern is
     * available and at least one fuel rod is required
     */
    boolean canRun(ItemStack configuredPattern, ReactorDisplayState displayState,
                                  ReactorInputFluidManagerI inputFluidManager, Level level, boolean assembled);

    /**
     * Updates the heat without running the reactor (case where it isn't ready).
     * Heat is only calculated if the full pattern (every required item, not
     * just fuel) is available; otherwise it drops back to zero.
     *
     * @return the calculated heat, also written to the configured pattern's tag
     */
    int updateHeatOnly(ItemStack configuredPattern, ReactorDisplayState displayState, BigFluidStack fluidStack,
                       int totalHeatRatio, ReactorControllerInventory inventory, Level level, boolean assembled);

    /**
     * Calculates the reactor's heat during normal operation and writes it to
     * the configured pattern's tag.
     *
     * @return the calculated heat
     */
    int calculateAndWriteHeat(ItemStack configuredPattern, BigFluidStack fluidStack, int totalHeatRatio,
                              ReactorControllerInventory inventory, Level level);
}
