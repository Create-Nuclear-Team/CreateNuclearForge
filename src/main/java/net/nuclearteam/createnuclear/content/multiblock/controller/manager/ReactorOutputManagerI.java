package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Interface exposing operations specific to reactor outputs.
 */
public interface ReactorOutputManagerI extends ReactorIOManager {
    /** Returns an immutable copy of tracked positions. */
    List<BlockPos> getBlocksPosition(Level level);

    /**
     * Répartit la rotation totale entre les sorties suivies et applique
     * la vitesse/l'arrêt correspondant à chaque `ReactorOutputEntity`.
     */
    void rotateOutputs(Level level, boolean assembled, int rotation);
}
