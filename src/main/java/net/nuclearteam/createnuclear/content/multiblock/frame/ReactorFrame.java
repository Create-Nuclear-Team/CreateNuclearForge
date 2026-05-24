package net.nuclearteam.createnuclear.content.multiblock.frame;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancementBehaviour;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReactorFrame extends Block implements IWrenchable {
    public static final Property<Part> PART = EnumProperty.create("part", Part.class);
    protected ReactorPattern pattern =  new ReactorPattern();
    public ReactorFrame(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
        super.createBlockStateDefinition(builder);
    }

    public enum Part implements StringRepresentable {
        START,
        MIDDLE,
        END,
        NONE
        ;

        @Override
        public @NotNull String getSerializedName() {
            return CreateNuclearLang.asId(name());
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction.Axis axis = context.getClickedFace().getAxis();

        if (axis == Direction.Axis.X || axis == Direction.Axis.Z) axis = Direction.Axis.Y;

        return this.defaultBlockState().setValue(PART, getType(this.defaultBlockState(), getRelativeTop(level, pos, axis), getRelativeBottom(level, pos, axis)));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) return;

        Direction.Axis axis = Direction.Axis.Y;
        Part part = getType(state, getRelativeTop(level, pos, axis), getRelativeBottom(level, pos, axis));

        if (state.getValue(PART) == part) return;

        state = state.setValue(PART, part);
        level.setBlock(pos, state, 3);
    }

    public BlockState getRelativeTop(Level level, BlockPos pos, Direction.Axis axis) {
        return level.getBlockState(pos.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE)));
    }

    public  BlockState getRelativeBottom(Level level, BlockPos pos, Direction.Axis axis) {
        return level.getBlockState(pos.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE)));
    }

    public Part getType(BlockState state, BlockState above, BlockState below) {
        boolean shapeAboveSame = above.is(state.getBlock());
        boolean shapeBelowSame = below.is(state.getBlock());

        if (shapeAboveSame && ! shapeBelowSame) return  Part.END;
        else if (!shapeAboveSame && shapeBelowSame) return  Part.START;
        else if (shapeAboveSame) return Part.MIDDLE;
        return Part.NONE;
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
        return super.rotate(state, level, pos, direction);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return super.mirror(state, mirror);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        pattern.findController(pos, level, true);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @javax.annotation.Nullable LivingEntity pPlacer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, pPlacer, stack);
        CNAdvancementBehaviour.setPlacedBy(level, pos, pPlacer);
    }

    @Override // called when the player destroys the block, with or without a tool
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        pattern.findController(pos, level, false);
    }
}
