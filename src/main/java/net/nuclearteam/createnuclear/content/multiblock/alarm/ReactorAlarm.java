package net.nuclearteam.createnuclear.content.multiblock.alarm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.nuclearteam.createnuclear.CNSoundEvents;

import javax.annotation.Nullable;

public class ReactorAlarm extends Block {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    // Ajuste selon ton son (durée de ton sample). 20 ticks = 1 seconde.
    private static final int SOUND_INTERVAL_TICKS = 720;

    private static final float VOLUME = 2.0F;
    private static final float PITCH  = 1.0F;

    public ReactorAlarm(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        boolean powered = ctx.getLevel().hasNeighborSignal(ctx.getClickedPos());
        return this.defaultBlockState().setValue(POWERED, powered);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        // Si le bloc est placé déjà alimenté → on démarre la boucle
        if (!level.isClientSide && state.getValue(POWERED)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;

        boolean poweredNow = level.hasNeighborSignal(pos);
        boolean poweredBefore = state.getValue(POWERED);

        if (poweredNow != poweredBefore) {
            level.setBlock(pos, state.setValue(POWERED, poweredNow), 3);

            // Passage OFF -> ON : on démarre la boucle
            if (poweredNow) {
                level.scheduleTick(pos, this, 1);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) return;

        // 🔁 Joue ton son custom ici
        level.playSound(
                null,
                pos,
                CNSoundEvents.REACTOR_ALARM.getMainEvent(),
                SoundSource.BLOCKS,
                VOLUME,
                PITCH
        );

        // Reboucle tant que POWERED
        level.scheduleTick(pos, this, SOUND_INTERVAL_TICKS);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

}
