package net.nuclearteam.createnuclear.content.enriching.campfire;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nuclearteam.createnuclear.CNBlockEntityTypes;
import net.nuclearteam.createnuclear.CNEffects;
import org.jetbrains.annotations.Nullable;

public class EnrichingCampfireBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, IBE<EnrichingCampfireBlockEntity> {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final boolean spawnParticles;
    private final int fireDamage;

    public EnrichingCampfireBlock(Properties propertie, boolean spawnParticles, int fireDamage) {
        super(propertie);
        this.spawnParticles = spawnParticles;
        this.fireDamage = fireDamage;
        this.registerDefaultState((BlockState) ((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(LIT, true)).setValue(WATERLOGGED, false)).setValue(FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(LIT).booleanValue() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
            ((LivingEntity) entity).addEffect(new MobEffectInstance(CNEffects.RADIATION.get(), 100, 0));
            //entity.hurt(level.damageSources().inFire(), this.fireDamage); // a remplacé
        }
        super.entityInside(state, level, pos, entity);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos blockPos;
        Level levelAccessor = context.getLevel();
        boolean bl = levelAccessor.getFluidState(blockPos = context.getClickedPos()).getType() == Fluids.WATER;
        return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(WATERLOGGED, bl)).setValue(LIT, !bl)).setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED).booleanValue()) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT).booleanValue()) {
            return;
        }
        if (random.nextInt(10) == 0) {
            level.playLocalSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5f + random.nextFloat(), random.nextFloat() * .7f + .6f, false);
        }
        if (this.spawnParticles && random.nextInt(5) == 0) {
            for (int i = 0; i < random.nextInt(1) + 1; ++i) {
                level.addParticle(ParticleTypes.LAVA, (double) pos.getX() + .5, (double) pos.getY() + .5, (double) pos.getZ() + .5, random.nextFloat() / 2.0f, 5.0E-5, random.nextFloat() / 2.0f);
            }
        }
    }

    public static void dowse(@Nullable Entity entity, LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity;
        if (level.isClientSide()) {
            for (int i = 0; i < 20; ++i) {
                EnrichingCampfireBlock.makeParticles((Level) level, pos);
            }
        }
        if ((blockEntity = level.getBlockEntity(pos)) instanceof EnrichingCampfireBlockEntity) {
            ((EnrichingCampfireBlockEntity) blockEntity).dowse();
        }
        level.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.getValue(BlockStateProperties.WATERLOGGED).booleanValue() && fluidState.getType() == Fluids.WATER) {
            boolean bl = state.getValue(LIT);
            if (bl) {
                if (!level.isClientSide()) {
                    level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                EnrichingCampfireBlock.dowse(null, level, pos, state);
            }
            level.setBlock(pos, (BlockState)((BlockState)state.setValue(WATERLOGGED, true)).setValue(LIT, false), 3);
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            return true;
        }
        return false;
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (!level.isClientSide && projectile.isOnFire() && projectile.mayInteract(level, blockPos) && !state.getValue(LIT).booleanValue() && !state.getValue(WATERLOGGED).booleanValue()) {
            level.setBlock(blockPos, (BlockState)state.setValue(BlockStateProperties.LIT, true), 11);
        }
    }

    public static void makeParticles(Level level, BlockPos pos) {
        RandomSource randomSource = level.getRandom();
        level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, (double)pos.getX() + 0.5 + randomSource.nextDouble() / 3.0 * (double)(randomSource.nextBoolean() ? 1 : -1), (double)pos.getY() + randomSource.nextDouble() + randomSource.nextDouble(), (double)pos.getZ() + 0.5 + randomSource.nextDouble() / 3.0 * (double)(randomSource.nextBoolean() ? 1 : -1), 0.0, 0.07, 0.0);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(state);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return (BlockState)state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, WATERLOGGED, FACING);
    }

    @Override
    public Class<EnrichingCampfireBlockEntity> getBlockEntityClass() {
        return EnrichingCampfireBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EnrichingCampfireBlockEntity> getBlockEntityType() {
        return CNBlockEntityTypes.ENRICHING_CAMPFIRE_BLOCK.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnrichingCampfireBlockEntity(CNBlockEntityTypes.ENRICHING_CAMPFIRE_BLOCK.get(), pos, state);
    }

    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level level, BlockState state, BlockEntityType<S> blockEntityType) {
        if (level.isClientSide) {
            if (state.getValue(LIT).booleanValue()) {
                return EnrichingCampfireBlock.createTickerHelper(blockEntityType, CNBlockEntityTypes.ENRICHING_CAMPFIRE_BLOCK.get(), EnrichingCampfireBlockEntity::particleTick);
            }
        }
        return null;
    }

    @Override
    public boolean isPathfindable(BlockState p_60475_, BlockGetter p_60476_, BlockPos p_60477_, PathComputationType p_60478_) {
        return false;
    }

    public static int getLight(BlockState state) {
        return state.getValue(LIT) ? 15 : 0;
    }
}
