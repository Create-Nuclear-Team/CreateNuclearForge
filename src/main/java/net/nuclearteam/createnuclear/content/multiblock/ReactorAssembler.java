package net.nuclearteam.createnuclear.content.multiblock;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.api.multiblock.BlockPattern;
import net.nuclearteam.createnuclear.api.multiblock.TypeMultiblock;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.frame.ReactorFrameEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputEntity;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancement;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;
import net.nuclearteam.createnuclear.foundation.utility.NotifyUtil;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

import java.util.List;

public final class ReactorAssembler {

    public static final int configRadius = CNConfigs.server().notify.warningDistance.get();
    public static final boolean configWarnAll = CNConfigs.server().notify.warnAllPlayers.get();

    private ReactorAssembler() {}

    public static void assemble(BlockPos pos, Level level) {
        ReactorControllerBlockEntity entity = getBlockEntity(level, pos);
        if (entity == null) return;

        BlockPattern<TypeMultiblock> result = CNMultiblock.REGISTRATE_MULTIBLOCK.findStructure(level, pos, entity);
        if (result == null) return;

        CreateNuclear.LOGGER.warn("ReactorAssembler#assemble id: {}, size: {}, name: {}",
                result.id(), result.data().getSize(), result.data().getName());

        sendMessageToPlayer(level, pos, CreateNuclearLang.translate("notification.reactor.assembled"), !entity.isAssembled());

        switch (result.data()) {
            case REACTOR_T1 -> entity.getAdvancement().awardPlayer(CNAdvancement.T1_REACTOR);
            case REACTOR_T2 -> entity.getAdvancement().awardPlayer(CNAdvancement.T2_REACTOR);
            case REACTOR_T3 -> entity.getAdvancement().awardPlayer(CNAdvancement.T3_REACTOR);
        }

        entity.setMultiblockSize(result.data().getSize());
        entity.setAssembled(true);
        entity.setMultiblockStructure(entity.getStructureBounds(pos, entity.getMultiblockSize(), entity.getMultiblockFacing()));

        findAndRegisterSpecialBlocks(entity.getMultiblockPos(), entity, level);
    }

    public static void disassemble(BlockPos pos, Level level) {
        ReactorControllerBlockEntity entity = getBlockEntity(level, pos);
        if (entity == null || !entity.isAssembled()) return;

        BlockPattern<TypeMultiblock> result = CNMultiblock.REGISTRATE_MULTIBLOCK.findStructure(level, pos, entity);
        if (result != null) return;

        sendMessageToPlayer(level, pos, CreateNuclearLang.translate("notification.reactor.disassembled"), true);

        entity.setAssembled(false);
        entity.removeIOAll();
    }

    public static void findAndRegisterSpecialBlocks(int[] reactorPos, ReactorControllerBlockEntity entity, Level level) {
        int xMin = reactorPos[0], xMax = reactorPos[1];
        int yMin = reactorPos[2], yMax = reactorPos[3];
        int zMin = reactorPos[4], zMax = reactorPos[5];

        final Block reactorOutputBlock = CNBlocks.REACTOR_OUTPUT.get();
        final Block reactorInputBlock = CNBlocks.REACTOR_INPUT.get();
        final Block reactorInputFluidBlock = CNBlocks.REACTOR_LIQUID_INPUT.get();
        final Block reactorAlarmBlock = CNBlocks.REACTOR_ALARM.get();
        final Block reactorFrameBlock = CNBlocks.REACTOR_FRAME.get();

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        int frameMinY = Integer.MAX_VALUE;
        int frameMaxY = Integer.MIN_VALUE;

        for (int y = yMin; y <= yMax; y++) {
            boolean isYBoundary = (y == yMin || y == yMax);
            for (int x = xMin; x <= xMax; x++) {
                boolean isXBoundary = (x == xMin || x == xMax);
                for (int z = zMin; z <= zMax; z++) {
                    boolean isZBoundary = (z == zMin || z == zMax);

                    if (!(isYBoundary || isXBoundary || isZBoundary)) continue;

                    mutablePos.set(x, y, z);
                    BlockState blockState = level.getBlockState(mutablePos);

                    if (blockState.is(reactorOutputBlock)) {
                        entity.addOutput(mutablePos.immutable());
                    } else if (blockState.is(reactorInputBlock)) {
                        entity.addInput(mutablePos.immutable());
                    } else if (blockState.is(reactorInputFluidBlock)) {
                        entity.addInputFluid(mutablePos.immutable());
                        if (level.getBlockEntity(mutablePos) instanceof ReactorFluidInputEntity fluidInput) {
                            fluidInput.applyReactorTierCapacity(entity.getMultiblockSize());
                        }
                    } else if (blockState.is(reactorAlarmBlock)) {
                        entity.addAlarm(mutablePos.immutable());
                    } else if (blockState.is(reactorFrameBlock)) {
                        if (level.getBlockEntity(mutablePos) instanceof ReactorFrameEntity frame) {
                            frame.setController(entity.getBlockPos());
                        }
                        frameMinY = Math.min(frameMinY, y);
                        frameMaxY = Math.max(frameMaxY, y);
                    }
                }
            }
        }

        if (frameMinY != Integer.MAX_VALUE) {
            entity.setFrameColumn(frameMinY, frameMaxY);
        }
    }

    private static ReactorControllerBlockEntity getBlockEntity(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ReactorControllerBlockEntity entity) {
            return entity;
        }
        return null;
    }

    @Deprecated
    private static List<Player> getPlayersInRadius(Level level, BlockPos center, double radius) {
        AABB box = new AABB(center).inflate(radius);
        return level.getEntitiesOfClass(Player.class, box);
    }

    private static void sendMessageToPlayer(Level level, BlockPos pos, LangBuilder component, boolean condition) {
        if (!condition) return;

        NotifyUtil.sendActionBar(level, pos,
                component,
                ChatFormatting.GOLD, configRadius, configWarnAll
        );
    }
}
