package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.VirtualReactorInputFluid;

import java.util.List;

public interface ReactorInputFluidManagerI extends ReactorIOManager {
    /**
     * Interface for managing reactor input fluids.
     * Implementations track input fluid sources and provide
     * accessors and extraction methods for reactor consumption.
     */
    /** Returns an immutable copy of tracked positions. */
    List<BlockPos> getBlocksPosition(Level level);

    /**
     * Returns a list of fluid handler capabilities for the tracked positions.
     * @param level the current world level
     */
    List<IFluidHandler> getFuildHandlers(Level level);

    /**
     * Returns a virtual aggregated inventory representing all tracked input fluids.
     * @param level the current world level
     */
    VirtualReactorInputFluid getInventory(Level level);

    /**
     * Attempt to extract the requested amount of fluid from tracked sources.
     * @param level current world level
     * @param fluidNeeded amount of fluid required
     * @return true if the requested amount was successfully extracted
     */
    boolean extractFluids(Level level, int fluidNeeded);
}
