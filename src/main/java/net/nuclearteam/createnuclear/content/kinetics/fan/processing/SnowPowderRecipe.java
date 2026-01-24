package net.nuclearteam.createnuclear.content.kinetics.fan.processing;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.nuclearteam.createnuclear.CNRecipeTypes;
import net.nuclearteam.createnuclear.content.kinetics.fan.processing.SnowPowderRecipe.SnowPowderWrapper;

public class SnowPowderRecipe extends ProcessingRecipe<SnowPowderWrapper> {
    public SnowPowderRecipe(ProcessingRecipeParams params) {
        super(CNRecipeTypes.SNOW_POWDER, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 12;
    }

    @Override
    public boolean matches(SnowPowderWrapper inv, Level worldIn) {
        if (inv.isEmpty())
            return false;
        return ingredients.get(0)
                .test(inv.getItem(0));
    }

    public static class SnowPowderWrapper extends RecipeWrapper {
        public SnowPowderWrapper() {
            super(new ItemStackHandler(1));
        }
    }
}
