package net.nuclearteam.createnuclear.foundation.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.nuclearteam.createnuclear.CNPackets;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.multiblock.controller.EventTriggerPacket;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public class EventTriggerBlock extends Block {
    public EventTriggerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            // Send a packet to all clients around this block within 16 blocks
            EventTriggerPacket packet = new EventTriggerPacket(100); // display for 100 ticks
            CreateNuclear.LOGGER.warn("hum EventTriggerBlock ? {}", packet);
            CNPackets.sendToNear(level, pos, 16, packet);
        }
        return InteractionResult.SUCCESS;
    }
}
