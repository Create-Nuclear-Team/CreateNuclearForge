package net.nuclearteam.createnuclear.content.multiblock.reinforced;

import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class ReinforcedGlassBlock extends GlassBlock {
    public ReinforcedGlassBlock(Properties properties) {
        super(properties);
    }

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
}
