package net.nuclearteam.createnuclear.content.multiblock.casing;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.ReactorInput;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;

import javax.annotation.Nullable;
import java.util.List;

public class ReactorCasing extends Block implements IWrenchable, IBE<ReactorCasingEntity> {
    private TypeBlock typeBlock;

    public ReactorCasing(Properties properties, TypeBlock tBlock) {
        super(properties);
        this.typeBlock = tBlock;
    }


    @Override // Called when the block is placed on the world
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        List<? extends Player> players = level.players();
        FindController(pos, level, players, true);
    }

    @Override // called when the player destroys the block, with or without a tool
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        List<? extends Player> players = level.players();
        FindController(pos, level, players, false);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        List<? extends Player> players = level.players();
        FindController(pos, level, players, false);
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

    // En attendant le controller pour verifier le pattern
    public ReactorControllerBlock FindController(BlockPos blockPos, Level level, List<? extends Player> players, boolean first){ // Function that checks the surrounding blocks in order
        BlockPos newBlock; // to find the controller and verify the pattern
        Vec3i pos = new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        for (int y = pos.getY()-3; y != pos.getY()+4; y+=1) {
            for (int x = pos.getX()-5; x != pos.getX()+5; x+=1) {
                for (int z = pos.getZ()-5; z != pos.getZ()+5; z+=1) {
                    newBlock = new BlockPos(x, y, z);
                    if (level.getBlockState(newBlock).is(CNBlocks.REACTOR_CONTROLLER.get())) { // verifying the pattern
                        ReactorControllerBlock controller = (ReactorControllerBlock) level.getBlockState(newBlock).getBlock();
                        controller.Verify(level.getBlockState(newBlock), newBlock, level, players, first);
                        ReactorControllerBlockEntity entity = controller.getBlockEntity(level, newBlock);
                        if (entity.created) {
                            return controller;
                        }
                    }
                }
            }
        }
        return null;
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
