package net.nuclearteam.createnuclear.foundation.data.recipe;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.api.data.recipe.EnrichedRecipeGen;

import java.util.function.Supplier;

public class CNEnrichedRecipeGen extends EnrichedRecipeGen {

    GeneratedRecipe
        ENRICHING_CAMPFIRES = convert(Items.CAMPFIRE, CNBlocks.ENRICHING_CAMPFIRE),
        ENRICHED_YELLOWCAKE = convert(() -> Ingredient.of(CNItems.YELLOWCAKE), () -> CNItems.ENRICHED_YELLOWCAKE)
    ;

    public GeneratedRecipe convert(ItemLike input, ItemLike result) {
        return convert(() -> Ingredient.of(input), () -> result);
    }

    public GeneratedRecipe convert(Supplier<Ingredient> input, Supplier<ItemLike> result) {
        return create(CreateNuclear.asResource(CatnipServices.REGISTRIES.getKeyOrThrow(result.get()
                .asItem())
                .getPath()),
                p -> p.withItemIngredients(input.get())
                        .output(result.get()));
    }

    public CNEnrichedRecipeGen(PackOutput output) {
        super(output, CreateNuclear.MOD_ID);
    }
}
