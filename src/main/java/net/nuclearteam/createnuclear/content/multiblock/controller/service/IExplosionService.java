package net.nuclearteam.createnuclear.content.multiblock.controller.service;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

public interface IExplosionService {
    void triggerExplosion(ServerLevel level, BlockPos controllerPos, int reactorSize, int uraniumRodCount,
                          int notifyRadius, boolean notifyWarnAll);

    default void triggerExplosion(ServerLevel level, BlockPos controllerPos, int reactorSize, int uraniumRodCount) {
        triggerExplosion(level, controllerPos, reactorSize,
                            uraniumRodCount, CNConfigs.server().notify.distanceOfWarning.get(),
                            CNConfigs.server().notify.warnAllPlayers.get()
        );
    }
}
