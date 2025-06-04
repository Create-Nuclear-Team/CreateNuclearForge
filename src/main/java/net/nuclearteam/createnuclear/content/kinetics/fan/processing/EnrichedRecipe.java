package net.nuclearteam.createnuclear.content.kinetics.fan.processing;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;

import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.nuclearteam.createnuclear.CNRecipeTypes;
import net.nuclearteam.createnuclear.content.kinetics.fan.processing.EnrichedRecipe.EnrichedWrapper;

@ParametersAreNonnullByDefault
public class EnrichedRecipe extends ProcessingRecipe<EnrichedWrapper> {

    public EnrichedRecipe(ProcessingRecipeParams params) {
        super(CNRecipeTypes.ENRICHED, params);
    }

    @Override
    public boolean matches(EnrichedWrapper inv, Level worldIn) {
        if (inv.isEmpty())
            return false;
        return ingredients.get(0)
                .test(inv.getItem(0));
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 12;
    }

    public static class EnrichedWrapper extends RecipeWrapper {
        public EnrichedWrapper() {
            super(new ItemStackHandler(1));
        }
    }

}
