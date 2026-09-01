package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Interface exposing operations specific to reactor outputs.
 */
public interface ReactorOutputManagerI extends ReactorIOManager {
    /** Returns an immutable copy of tracked positions. */
    List<BlockPos> getBlocksPosition(Level level);

    /**
     * Distributes the total rotation across the tracked outputs and applies
     * the corresponding speed/stop to each {@code ReactorOutputEntity}.
     */
    void rotateOutputs(Level level, boolean assembled, int rotation);
}
