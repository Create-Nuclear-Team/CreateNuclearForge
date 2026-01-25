package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.VirtualReactorInputFluid;

import java.util.List;

public interface ReactorInputFluidManagerI extends ReactorIOManager {
    /** Returns an immutable copy of tracked positions. */
    List<BlockPos> getBlocksPosition(Level level);

    List<IFluidHandler> getFuildHandlers(Level level);

    VirtualReactorInputFluid getInventory(Level level);

    boolean extractFluids(Level level, int fluidNeeded);
}
