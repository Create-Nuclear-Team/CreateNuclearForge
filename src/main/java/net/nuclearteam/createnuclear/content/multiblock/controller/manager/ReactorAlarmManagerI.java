package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.util.List;

public interface ReactorAlarmManagerI extends ReactorIOManager {
    /** * Retourne une copie immuable des positions d'alarmes valides dans le monde actuel.
     */
    List<BlockPos> getBlocksPosition(Level level);
}