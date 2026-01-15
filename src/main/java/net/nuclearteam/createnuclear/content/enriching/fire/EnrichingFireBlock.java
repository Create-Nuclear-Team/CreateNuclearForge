package net.nuclearteam.createnuclear.content.enriching.fire;

import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.nuclearteam.createnuclear.CNTags.CNBlockTags;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SuppressWarnings({"deprecation"})
public class EnrichingFireBlock extends BaseFireBlock {

    public EnrichingFireBlock(Properties properties, float fireDamage) {
        super(properties, fireDamage);
    }

    public BlockState getStateForPlacement() {
        return this.defaultBlockState();
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return this.canSurvive(pState, pLevel, pCurrentPos)
                ? this.getStateForPlacement()
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
      return worldIn.getBlockState(blockpos).isFaceSturdy(worldIn, blockpos, Direction.UP) || this.isValidFireLocation(worldIn, pos);
    }

    private boolean isValidFireLocation(BlockGetter pLevel, BlockPos pPos) {
      for(Direction direction : Direction.values()) {
         if (this.canCatchFire(pLevel, pPos.relative(direction), direction.getOpposite())) {
            return true;
         }
      }

      return false;
   }

   public boolean canCatchFire(BlockGetter world, BlockPos pos, Direction face) {
      return world.getBlockState(pos).isFlammable(world, pos, face);
   }

    @Override
    protected boolean canBurn(BlockState p_49284_) {
        return true;
    }

    public static boolean canSurviveOnBlock(BlockState pState) {
        return pState.is(CNBlockTags.ENRICHING_FIRE_BASE_BLOCKS.tag);
    }

    public static NonNullUnaryOperator<Properties> getLight() {
        return p -> p.lightLevel(a -> 15);
    }
}
