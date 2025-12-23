package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;

import java.util.function.BiFunction;

/**
 * Interface exposing operations specific to reactor outputs.
 * The main responsibility is to distribute an amount of energy (SU)
 * among valid outputs and use an `inserter` to apply quantities to each
 * `ReactorOutputEntity`.
 */
public interface ReactorOutputManagerI {
    /**
     * Distributes `totalSU` among valid outputs in `level`.
     * The `inserter` receives the target entity and the proposed amount and
     * must return the amount not accepted (>= 0).
     * Returns the remaining amount that was not inserted.
     */
    double distributeSU(double totalSU, Level level, BiFunction<ReactorOutputEntity, Double, Double> inserter);
}
