package net.nuclearteam.createnuclear.content.multiblock;

import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.BiConsumer;

public class MultiblockHelpers {
    private static final ReactorPattern pattern = new ReactorPattern();

    public static void handleOnPlace(BlockPos pos, Level level, BiConsumer<ReactorControllerBlockEntity, BlockPos> register) {
        List<? extends Player> players = level.players();
        pattern.FindController(pos, level, players, true);

        BlockPos controllerPos = pattern.findControllerPos(pos, level, players, true);
        if (controllerPos != null) {
            ReactorControllerBlockEntity controllerBlockEntity = (ReactorControllerBlockEntity) level.getBlockEntity(controllerPos);
            if (controllerBlockEntity != null) {
                register.accept(controllerBlockEntity, pos);
            }
        }
    }

    public static void handleRemoval(BlockPos pos, Level level, BiConsumer<ReactorControllerBlockEntity, BlockPos> remover) {
        List<? extends Player> players = level.players();
        pattern.FindController(pos, level, players, false);

        BlockPos controllerPos = pattern.findControllerPos(pos, level, players, true);
        if (controllerPos != null) {
            ReactorControllerBlockEntity controllerBlockEntity = (ReactorControllerBlockEntity) level.getBlockEntity(controllerPos);
            if (controllerBlockEntity != null) {
                remover.accept(controllerBlockEntity, pos);
            }
        }
    }
}
