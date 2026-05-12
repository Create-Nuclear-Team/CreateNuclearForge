package net.nuclearteam.createnuclear.content.multiblock.reinforced;

import com.simibubi.create.content.decoration.palettes.ConnectedGlassBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancementBehaviour;

import javax.annotation.Nullable;

public class ReinforcedGlassBlock extends ConnectedGlassBlock {
    public ReinforcedGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity pPlacer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, pPlacer, stack);
        CNAdvancementBehaviour.setPlacedBy(level, pos, pPlacer);
    }
}
