package net.nuclearteam.createnuclear.content.multiblock.core;

import lib.multiblock.SimpleMultiBlockAislePatternBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.GameRules;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNEntityType;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.content.multiblock.casing.ReactorCasingEntity;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

import static net.nuclearteam.createnuclear.content.multiblock.CNMultiblock.*;

@SuppressWarnings({"unused"})
public class ReactorCoreEntity extends ReactorCasingEntity {

    private int countdownTicks = 0;
    private boolean hasExploded = false;

    public ReactorCoreEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide() || hasExploded) return;

        BlockPos controllerPos = getBlockPosForReactor();
        int heat = 0;
        if (!(level.getBlockEntity(controllerPos) instanceof ReactorControllerBlockEntity reactorController)) {
            countdownTicks = 0;
            return;
        }
        if (reactorController.getConfiguredPattern() == null) {
            countdownTicks = 0;
            return;
        }
        heat = (int) reactorController.getConfiguredPattern()
                .getOrCreateTag()
                .getDouble("heat");

        if (IHeat.HeatLevel.of(heat) == IHeat.HeatLevel.DANGER) {

            countdownTicks++;

            // 300 ticks = 15 secondes
            if (countdownTicks >= 300) {
                hasExploded = true;

                triggerNuclearExplosion(
                        (ServerLevel) level,
                        getBlockPos(),
                        reactorController.getBigFuelItem().count
                );

                // Supprime le cœur (optionnel mais logique)
                level.removeBlock(getBlockPos(), false);

        /*if (level.getBlockEntity(controllerPos) instanceof ReactorControllerBlockEntity reactorController) {
            int heat = (int) reactorController.getConfiguredPattern().getOrCreateTag().getDouble("heat");
            if (IHeat.HeatLevel.of(heat) == IHeat.HeatLevel.DANGER) {
                if (countdownTicks >= CNConfigs.server().explode.time.get()) { // 300 ticks = 15 secondes
                    explodeReactorCore(level, getBlockPos());
                } else {
                    countdownTicks++;
                }
            } else {
                countdownTicks = 0; // Reset the countdown if the heat level is not in danger*/
            }

        } else {
            countdownTicks = 0;
        }
    }

    /**
     * Déclenche l’explosion nucléaire du réacteur
     */
    private void triggerNuclearExplosion(ServerLevel level, BlockPos pos, int uraniumRodCount) {

        /*
        NuclearExplosionEntity explosion =
                new NuclearExplosionEntity(
                        CNEntityType.NUCLEAR_EXPLOSION.get(),
                        level
                );

        explosion.setPos(
                pos.getX() + 0.5D,
                pos.getY() + 10.0D,
                pos.getZ() + 0.5D
        );

        // Mapping uranium -> puissance
        float size = Mth.clamp(uraniumRodCount * 0.5F, 1.0F, 6.0F);
        explosion.setSize(size);

        // Respect gamerule mobGriefing
        boolean griefing =
                level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);

        explosion.setNoGriefing(!griefing);
        explosion.setIntentionalGameDesign(false);

        level.addFreshEntity(explosion);
         */
    }

    /* ===================== MULTIBLOCK ===================== */

   /* private static BlockPos FindController(char character) {
        return SimpleMultiBlockAislePatternBuilder.start()
                .aisle(AAAAA, AAAAA, AAAAA, AAAAA, AAAAA)
                .aisle(AABAA, ADADA, BACAB, ADADA, AABAA)
                .aisle(AABAA, ADADA, BACAB, ADADA, AABAA)
                .aisle(AAIAA, ADADA, BACAB, ADADA, AAAA)
                .aisle(AABAA, ADADA, BACAB, ADADA, AABAA)
                .aisle(AABAA, ADADA, BACAB, ADADA, AABAA)
                .aisle(AAAAA, AAAAA, AAAAA, AAAAA, AAOAA)
                .where('A', a -> a.getState().is(CNBlocks.REACTOR_CASING.get()))
                .where('B', a -> a.getState().is(CNBlocks.REACTOR_FRAME.get()))
                .where('C', a -> a.getState().is(CNBlocks.REACTOR_CORE.get()))
                .where('D', a -> a.getState().is(CNBlocks.REACTOR_COOLER.get()))
                .where('*', a -> a.getState().is(CNBlocks.REACTOR_CONTROLLER.get()))
                .where('O', a -> a.getState().is(CNBlocks.REACTOR_OUTPUT.get()))
                .where('I', a -> a.getState().is(CNBlocks.REACTOR_INPUT.get()))
                .getDistanceController(character);
    }*/

    private BlockPos getBlockPosForReactor() {
        BlockPos origin = getBlockPos();

        int[][][] directions = {
                {{0, 2, 2}, {0, 1, 2}, {0, 0, 2}, {0, -1, 2}, {0, -2, 2}},   // NORTH
                {{0, 2, -2}, {0, 1, -2}, {0, 0, -2}, {0, -1, -2}, {0, -2, -2}}, // SOUTH
                {{2, 2, 0}, {2, 1, 0}, {2, 0, 0}, {2, -1, 0}, {2, -2, 0}},   // EAST
                {{-2, 2, 0}, {-2, 1, 0}, {-2, 0, 0}, {-2, -1, 0}, {-2, -2, 0}} // WEST
        };

        for (int[][] direction : directions) {
            for (int[] dir : direction) {
                BlockPos check = origin.offset(dir[0], dir[1], dir[2]);
                if (level.getBlockState(check).is(CNBlocks.REACTOR_CONTROLLER.get())) {
                    return check;
                }
            }
        }

        return origin;
    }
}
