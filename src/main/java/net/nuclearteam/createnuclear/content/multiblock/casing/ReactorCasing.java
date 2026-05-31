package net.nuclearteam.createnuclear.content.multiblock.casing;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.nuclearteam.createnuclear.CNBlockEntityTypes;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.content.multiblock.input.item.ReactorInput;
import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancementBehaviour;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings({"deprecation", "unused"})
public class ReactorCasing extends Block implements IWrenchable, IBE<ReactorCasingEntity> {
    private final TypeBlock typeBlock;
    protected ReactorPattern pattern =  new ReactorPattern();

    public ReactorCasing(Properties properties, TypeBlock tBlock) {
        super(properties);
        this.typeBlock = tBlock;
    }


    @Override // Called when the block is placed on the world
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // change nearby chunks' biomes to the mod biome (radius in blocks)
        pattern.findController(pos, level, true);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity pPlacer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, pPlacer, stack);
        CNAdvancementBehaviour.setPlacedBy(level, pos, pPlacer);
    }

    @Override // called when the player destroys the block, with or without a tool
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        pattern.findController(pos, level, false);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        pattern.findController(pos, level, false);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player.getItemInHand(InteractionHand.OFF_HAND).is(Blocks.HOPPER.asItem())) {
            level.setBlock(pos, CNBlocks.REACTOR_INPUT.getDefaultState().setValue(ReactorInput.FACING, context.getClickedFace()), 2);
            player.sendSystemMessage(Component.translatable("reactor.update.casing.input"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Class<ReactorCasingEntity> getBlockEntityClass() {
        return ReactorCasingEntity.class;
    }

    @Override
    public BlockEntityType<? extends ReactorCasingEntity> getBlockEntityType() {
        return switch (typeBlock) {
            case CORE -> CNBlockEntityTypes.REACTOR_CORE.get();
            case CASING -> CNBlockEntityTypes.REACTOR_CASING.get();
        };

    }

    public enum TypeBlock implements StringRepresentable {
        CASING,
        CORE,
        ;

        @Override
        public String getSerializedName() {
            return CreateNuclearLang.asId(name());
        }
    }
}
