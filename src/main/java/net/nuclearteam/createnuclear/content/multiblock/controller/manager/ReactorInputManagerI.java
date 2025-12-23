package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Interface exposing operations specific to reactor inputs.
 * Currently provides access to `IItemHandler` instances at resolved input positions.
 */
public interface ReactorInputManagerI {
    /**
     * Retrieves valid item handlers for the given `level`.
     * May return an empty list if no valid positions exist.
     */
    List<IItemHandler> getItemHandlers(Level level);
}
