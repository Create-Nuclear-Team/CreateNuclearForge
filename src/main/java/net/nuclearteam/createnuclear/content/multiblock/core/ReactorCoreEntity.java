package net.nuclearteam.createnuclear.content.multiblock.core;

import lib.multiblock.SimpleMultiBlockAislePatternBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.content.multiblock.casing.ReactorCasingEntity;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

import static net.nuclearteam.createnuclear.content.multiblock.CNMultiblock.*;

@SuppressWarnings({"unused"})
public class ReactorCoreEntity extends ReactorCasingEntity {
    private int countdownTicks = 0;

    public ReactorCoreEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide()) return;

        BlockPos controllerPos = getBlockPosForReactor();
        if (level.getBlockEntity(controllerPos) instanceof ReactorControllerBlockEntity reactorController) {
            int heat = (int) reactorController.configuredPattern.getOrCreateTag().getDouble("heat");
            if (IHeat.HeatLevel.of(heat) == IHeat.HeatLevel.DANGER) {
                if (countdownTicks >= CNConfigs.server().explode.time.get()) { // 300 ticks = 15 secondes
                    explodeReactorCore(level, getBlockPos());
                } else {
                    countdownTicks++;
                }
            } else {
                countdownTicks = 0; // Reset the countdown if the heat level is not in danger
            }
        }
    }

    private void explodeReactorCore(Level world, BlockPos pos) {
        level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 20F, Level.ExplosionInteraction.BLOCK);
    }

    private BlockPos getBlockPosForReactor() {
        BlockPos posController = getBlockPos();
        BlockPos posInput = new BlockPos(posController.getX(), posController.getY(), posController.getZ());

        int[][][] directions = {
                {{0, 2, 2}, {0, 1, 2}, {0, 0, 2}, {0, -1, 2}, {0, -2, 2}}, // NORTH
                {{0, 2, -2}, {0, 1, -2}, {0, 0, -2}, {0, -1, -2}, {0, -2, -2}}, // SOUTH

                {{2, 2, 0}, {2, 1, 0}, {2, 0, 0}, {2, -1, 0}, {2, -2, 0}}, // EAST
                {{-2, 2, 0}, {-2, 1, 0}, {-2, 0, 0}, {-2, -1, 0}, {-2, -2, 0}} // WEST
        };


        for (int[][] direction : directions) {
            for (int[] dir : direction) {
                BlockPos newPos = posController.offset(dir[0], dir[1], dir[2]);
                if (level.getBlockState(newPos).is(CNBlocks.REACTOR_CONTROLLER.get())) {
                    posInput = newPos;
                    break;
                }
            }
        }

        return posInput;
    }
}
