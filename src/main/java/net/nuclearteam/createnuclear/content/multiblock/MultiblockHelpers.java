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

    /**
     * Utility helpers for handling multiblock placement and removal events.
     * These helpers locate the controller for a given part position and
     * invoke provided callbacks to register or remove parts.
     */
    public static void handleOnPlace(BlockPos pos, Level level, BiConsumer<ReactorControllerBlockEntity, BlockPos> register) {
        /**
         * Called when a multiblock part is placed. Locates the controller and
         * invokes {@code register} with the controller and placed part position.
         */
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
        /**
         * Called when a multiblock part is removed. Locates the controller and
         * invokes {@code remover} with the controller and removed part position.
         */
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

    public static ReactorControllerBlockEntity getControllerForPart(Level level, BlockPos pos) {
        /**
         * Returns the controller entity for the given part position, or null
         * if no valid controller is found.
         */
        List<? extends Player> players = level.players();
        BlockPos controllerPos = pattern.findControllerPos(pos, level, players, true);
        if (controllerPos != null) {
            return  (ReactorControllerBlockEntity) level.getBlockEntity(controllerPos);
        }

        return null;
    }
}
