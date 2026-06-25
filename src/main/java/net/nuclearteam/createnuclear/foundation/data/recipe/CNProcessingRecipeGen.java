package net.nuclearteam.createnuclear.foundation.data.recipe;

import com.simibubi.create.Create;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.createmod.catnip.platform.CatnipServices;
import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;


public abstract class CNProcessingRecipeGen extends RecipeProvider {

    protected static final List<ProcessingRecipeGen> GENERATORS = new ArrayList<>();

    public static void registerAll(DataGenerator gen, PackOutput output) {
        GENERATORS.add(new CNCompactingRecipeGen(output));
        GENERATORS.add(new CNItemApplicationRecipeGen(output));
        GENERATORS.add(new CNCrushingRecipeGen(output));
        GENERATORS.add(new CNMixingRecipeGen(output));
        GENERATORS.add(new CNPressingRecipeGen(output));
        GENERATORS.add(new CNEnrichedRecipeGen(output));
        GENERATORS.add(new CNSnowPowderRecipeGen(output));
        GENERATORS.add(new CNWashingRecipeGen(output));
        GENERATORS.add(new CNDeployingRecipeGen(output));

        gen.addProvider(true, new DataProvider() {

            @Override
            public String getName() {
                return "CreateNuclear's Processing Recipes";
            }

            @Override
            public CompletableFuture<?> run(CachedOutput dc) {
                return CompletableFuture.allOf(GENERATORS.stream()
                        .map(gen -> gen.run(dc))
                        .toArray(CompletableFuture[]::new));
            }
        });
    }



    public CNProcessingRecipeGen(PackOutput output) {
        super(output);
    }
}
