package net.nuclearteam.createnuclear.content.multiblock.controller;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.nuclearteam.createnuclear.*;
import net.nuclearteam.createnuclear.api.multiblock.BlockPattern;
import net.nuclearteam.createnuclear.api.multiblock.TypeMultiblock;
import net.nuclearteam.createnuclear.content.multiblock.CNMultiblock;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.FluidLockManager;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.PersistentFluidLocks;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutput;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;
import net.nuclearteam.createnuclear.foundation.block.HorizontalDirectionalReactorBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SuppressWarnings("deprecation")
public class ReactorControllerBlock extends HorizontalDirectionalReactorBlock implements IWrenchable, IBE<ReactorControllerBlockEntity> {
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

    public ReactorControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(ASSEMBLED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ASSEMBLED, false);
    }
    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving) {
        withBlockEntityDo(worldIn, pos, be -> be.setAssembled(false));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide)
            return InteractionResult.SUCCESS;

        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (!(blockEntity instanceof ReactorControllerBlockEntity controllerBlockEntity)) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(handIn);
        if (heldItem.is(Items.PAPER)) {
            withBlockEntityDo(worldIn, pos, ReactorControllerBlockEntity::test);
        }

        if (!state.getValue(ASSEMBLED)) {
            player.sendSystemMessage(Component.translatable("reactor.info.assembled.none").withStyle(ChatFormatting.RED));
        }
        else {

            if (heldItem.is(CNItems.REACTOR_BLUEPRINT.get()) && controllerBlockEntity.getInventoryObject().getItem(0).isEmpty()){
                withBlockEntityDo(worldIn, pos, be -> {
                    be.getInventoryObject().setStackInSlot(0, heldItem);
                    be.setConfiguredPattern(heldItem);

                    player.setItemInHand(handIn, ItemStack.EMPTY);
                });
                return InteractionResult.SUCCESS;

            }
            else if (heldItem.isEmpty() && !controllerBlockEntity.getInventoryObject().getItem(0).isEmpty()) {
                withBlockEntityDo(worldIn, pos, be -> {
                    player.setItemInHand(handIn, be.getInventoryObject().getItem(0));
                    be.getInventoryObject().setStackInSlot(0, ItemStack.EMPTY);
                    be.setConfiguredPattern(ItemStack.EMPTY);
                    be.setTotal(0.0);
                    be.rotate(be.getBlockState(), be.getLevel(), 0);
                    be.notifyUpdate();
                });
                state.setValue(ASSEMBLED, false);
                return InteractionResult.SUCCESS;

            }
            else if (!heldItem.isEmpty() && !controllerBlockEntity.getInventoryObject().getItem(0).isEmpty()) {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
            return;

        withBlockEntityDo(worldIn, pos, be -> ItemHelper.dropContents(worldIn, pos, be.getInventoryObject()));
        worldIn.removeBlockEntity(pos);

        ReactorControllerBlock controller = (ReactorControllerBlock) state.getBlock();

        controller.Rotate(state, pos.below(3), worldIn, 0);
        if (!worldIn.isClientSide && worldIn instanceof ServerLevel serverLevel) {
            PersistentFluidLocks.get(serverLevel).clearLock(pos);
        } else FluidLockManager.clearLock(pos);

        List<? extends Player> players = worldIn.players();
        for (Player p : players) {
            p.sendSystemMessage(Component.translatable("reactor.info.assembled.destroyer"));
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (state.getValue(ASSEMBLED))
            return;
        List<? extends Player> players = level.players();
        ReactorControllerBlock controller = (ReactorControllerBlock) state.getBlock();
        controller.Verify(state, pos, level, players, true);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        ReactorControllerBlock controller = (ReactorControllerBlock) state.getBlock();
        ReactorControllerBlockEntity entity = controller.getBlockEntity(level, pos);
        if (!entity.isAssembled()) return;
        controller.Rotate(state, pos.below(3), level, 0);
        List<? extends Player> players = level.players();
        for (Player p : players) {
            p.sendSystemMessage(Component.translatable("reactor.info.assembled.creator"));
        }
//        entity.removeIOAll();
    }

    // this is the Function that verifies if the pattern is correct (as a test, we added the energy output)
    public void Verify(BlockState state, BlockPos pos, Level level, List<? extends Player> players, boolean create){
        ReactorControllerBlock controller = (ReactorControllerBlock) level.getBlockState(pos).getBlock();
        ReactorControllerBlockEntity entity = controller.getBlockEntity(level, pos);
        if (entity == null) return;
        BlockPattern<TypeMultiblock> result = CNMultiblock.REGISTRATE_MULTIBLOCK.findStructure(level, pos, entity); // control the pattern
        if (result != null) { // the pattern is correct
            CreateNuclear.LOGGER.warn("Verify@BlockPattern<TypeMultiblock> id: {}, data<TypeMultiblock>$getSize: {}, data<TypeMultiblock>$getName: {}", result.id(), result.data().getSize(), result.data().getName());
//            entity.removeIOAll();
            for (Player player : players) {
                if (create && !entity.isAssembled()) {
                    player.sendSystemMessage(Component.translatable("reactor.info.assembled.creator"));
                    level.setBlockAndUpdate(pos, state.setValue(ASSEMBLED, true));
                    entity.setMultiblockSize(result.data().getSize());
                    entity.setAssembled(true);

                    entity.setMultiblockStructure(entity.getStructureBounds(pos, entity.getMultiblockSize(), entity.getMultiblockFacing()));
                    // Register existing special blocks (inputs/outputs) so the controller
                    // detects ReactorInput/ReactorOutput placed before the controller.
                    FindSpecialBlocksInReactor(entity.getMultiblockPos(), entity, level);
                }
            }
            return;
        }

        // the pattern is incorrect
        if (!create && entity.isAssembled()) {
            for (Player player : players) {
                player.sendSystemMessage(Component.translatable("reactor.info.assembled.destroyer"));
            }
            level.setBlockAndUpdate(pos, state.setValue(ASSEMBLED, false));
            entity.setAssembled(false);
            entity.removeIOAll();
            Rotate(state, pos.below(3), level, 0);
        }
    }
    public void Rotate(BlockState state, BlockPos pos, Level level, int rotation) {
        if (level.getBlockState(pos).is(CNBlocks.REACTOR_OUTPUT.get())) {
            ReactorOutput block = (ReactorOutput) level.getBlockState(pos).getBlock();
            ReactorOutputEntity entity = block.getBlockEntityType().getBlockEntity(level, pos);

            if (state.getValue(ASSEMBLED) && rotation != 0) { // Starting the energy
                entity.speed = rotation;
                entity.setSpeed(Math.abs(entity.speed));
            } else { // stopping the energy

                entity.setSpeed(0);
                entity.speed = 0;
            }
            entity.updateSpeed = true;
            entity.updateGeneratedRotation();

            entity.setSpeed(rotation);
        }
    }

    @Override
    public Class<ReactorControllerBlockEntity> getBlockEntityClass() {
        return ReactorControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ReactorControllerBlockEntity> getBlockEntityType() {
        return CNBlockEntityTypes.REACTOR_CONTROLLER.get();
    }

    public void FindSpecialBlocksInReactor(int[] reactorPos, ReactorControllerBlockEntity controllerBlockEntity, Level level){
        int xMin = reactorPos[0], xMax = reactorPos[1];
        int yMin = reactorPos[2], yMax = reactorPos[3];
        int zMin = reactorPos[4], zMax = reactorPos[5];

        final Block reactorOutputBlock = CNBlocks.REACTOR_OUTPUT.get();
        final Block reactorInputBlock = CNBlocks.REACTOR_INPUT.get();
        final Block reactorInputFluidBlock = CNBlocks.REACTOR_LIQUID_INPUT.get();
        final Block reactorAlarmBlock = CNBlocks.REACTOR_ALARM.get();

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int y = yMin; y <= yMax; y++) {
            boolean isYBoundary = (y == yMin || y == yMax);
            for (int x = xMin; x <= xMax; x++) {
                boolean isXBoundary = (x == xMin || x == xMax);
                for (int z = zMin; z <= zMax; z++) {
                    boolean isZBoundary = (z == zMin || z == zMax);

                    if (!(isYBoundary || isXBoundary || isZBoundary)) {
                        continue;
                    }

                    mutablePos.set(x, y, z);

                    BlockState state = level.getBlockState(mutablePos);

                    if (state.is(reactorOutputBlock)) {
                        controllerBlockEntity.addOutput(mutablePos.immutable());
                    }
                    else if (state.is(reactorInputBlock)) {
                        controllerBlockEntity.addInput(mutablePos.immutable());
                    }
                    else if (state.is(reactorInputFluidBlock)) {
                        controllerBlockEntity.addInputFluid(mutablePos.immutable());
                    } else if (state.is(reactorAlarmBlock)) {
                        controllerBlockEntity.addAlarm(mutablePos.immutable());
                    }
                }
            }
        }
    }
}
