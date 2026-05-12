package net.nuclearteam.createnuclear.content.multiblock.alarm;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.nuclearteam.createnuclear.CNBlockEntityTypes;
import net.nuclearteam.createnuclear.content.multiblock.MultiblockHelpers;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;

import javax.annotation.Nullable;

public class ReactorAlarm extends Block implements IBE<ReactorAlarmEntity> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    protected ReactorPattern pattern = new ReactorPattern();

    public ReactorAlarm(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    // AJOUT : Inscription au multiblock lors de la pose
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        MultiblockHelpers.handleOnPlace(pos, level, ReactorControllerBlockEntity::addAlarm);
    }

    // AJOUT : Désinscription au multiblock lors de la destruction par un joueur
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        MultiblockHelpers.handleRemoval(pos, level, ReactorControllerBlockEntity::removeAlarm);
    }

    // CORRECTION : Désinscription au multiblock lors du retrait (piston, explosion, etc.)
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        MultiblockHelpers.handleRemoval(pos, level, ReactorControllerBlockEntity::removeAlarm);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(POWERED, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;
        boolean poweredNow = level.hasNeighborSignal(pos);
        if (state.getValue(POWERED) != poweredNow) {
            level.setBlock(pos, state.setValue(POWERED, poweredNow), 3);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public Class<ReactorAlarmEntity> getBlockEntityClass() {
        return ReactorAlarmEntity.class;
    }

    @Override
    public BlockEntityType<? extends ReactorAlarmEntity> getBlockEntityType() {
        return CNBlockEntityTypes.REACTOR_ALARM.get();
    }
}