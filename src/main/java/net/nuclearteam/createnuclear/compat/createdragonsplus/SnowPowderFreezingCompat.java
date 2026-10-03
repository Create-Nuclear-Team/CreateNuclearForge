package net.nuclearteam.createnuclear.compat.createdragonsplus;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.CNRecipeTypes;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.content.kinetics.fan.processing.SnowPowderRecipe;
import net.nuclearteam.createnuclear.content.kinetics.fan.processing.SnowPowderRecipe.SnowPowderWrapper;
import plus.dragons.createdragonsplus.common.kinetics.fan.freezing.FreezingRecipe;
import plus.dragons.createdragonsplus.integration.CDPIntegrationContributions;
import plus.dragons.createdragonsplus.integration.CDPIntegrationContributions.StandardFanProcessingCompat;

import java.util.List;
import java.util.Optional;

public class SnowPowderFreezingCompat implements StandardFanProcessingCompat<FreezingRecipe> {
    private static final SnowPowderWrapper WRAPPER = new SnowPowderWrapper();

    public static void register() {
        CDPIntegrationContributions.registerFreezingCompat(new SnowPowderFreezingCompat());
    }

    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        return CNTags.CNBlockTags.FAN_PROCESSING_CATALYSTS_SNOW_POWDER.matches(level.getBlockState(pos));
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        return findRecipe(stack, level).isPresent();
    }

    @Override
    public Optional<List<ItemStack>> process(ItemStack stack, Level level) {
        return findRecipe(stack, level)
                .map(holder -> RecipeApplier.applyRecipeOn(level, stack, holder, true));
    }

    @Override
    public void gatherJeiRecipes(RecipeManager manager, List<FreezingRecipe> recipes) {
        manager.<SnowPowderWrapper, SnowPowderRecipe>getAllRecipesFor(CNRecipeTypes.SNOW_POWDER.getType())
                .forEach(holder -> recipes.add(FreezingRecipe.builder(holder.getId())
                        .withItemIngredients(holder.getIngredients())
                        .withItemOutputs(holder.getRollableResults().toArray(ProcessingOutput[]::new))
                        .build()));
    }

    private static Optional<SnowPowderRecipe> findRecipe(ItemStack stack, Level level) {
        WRAPPER.setItem(0, stack);
        return CNRecipeTypes.SNOW_POWDER.find(WRAPPER, level);
    }
}
