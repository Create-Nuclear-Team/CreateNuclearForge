package net.nuclearteam.createnuclear.foundation.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class CNCrushingRecipeGen extends CNProcessingRecipeGen {

    GeneratedRecipe
        COAL_DUST = create(() -> Items.COAL, b -> b.duration(250)
            .output(.50f, CNItems.COAL_DUST)
        ),

        CHARCOAL_DUST = create(() -> Items.CHARCOAL, b -> b.duration(250)
            .output(.50f, CNItems.COAL_DUST)
        ),

        GRANITE_URANIUM_POWDER = create(() -> Items.GRANITE, b -> b.duration(250)
            .output(.5f, CNItems.URANIUM_POWDER)
            .output(1f, Blocks.RED_SAND)
        ),

        FIX_RAW_URANIUM_FOR_FORGE = createFix(CreateNuclear.MOD_ID, () -> AllItems.CRUSHED_URANIUM::get, b -> b
                .duration(255)
                .output(1, CNItems.URANIUM_POWDER, 9)
        ),
        RAW_URANIUM_BLOCK = create(() -> CNBlocks.RAW_URANIUM_BLOCK, b -> b.duration(250)
            .output(1, CNItems.URANIUM_POWDER,81))
    ;

    public CNCrushingRecipeGen(PackOutput generator) {
        super(generator);
    }

    @Override
    protected AllRecipeTypes getRecipeType() {
        return AllRecipeTypes.CRUSHING;
    }

    protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(String namespace,
                                                                     Supplier<ItemLike> singleIngredient, UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
        ProcessingRecipeSerializer<T> serializer = getSerializer();
        GeneratedRecipe generatedRecipe = c -> {
            ItemLike itemLike = singleIngredient.get();
            transform
                    .apply(new ProcessingRecipeBuilder<>(serializer.getFactory(),
                            new ResourceLocation(namespace, CatnipServices.REGISTRIES.getKeyOrThrow(itemLike.asItem())
                                    .getPath())).withItemIngredients(Ingredient.of(itemLike)))
                    .build(c);
        };
        all.add(generatedRecipe);
        return generatedRecipe;
    }

    protected <T extends ProcessingRecipe<?>> GeneratedRecipe createFix(String namespace,
                                                                     Supplier<ItemLike> singleIngredient, UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
        ProcessingRecipeSerializer<T> serializer = getSerializer();
        GeneratedRecipe generatedRecipe = c -> {
            ItemLike itemLike = singleIngredient.get();
            transform
                    .apply(new ProcessingRecipeBuilder<>(serializer.getFactory(),
                            new ResourceLocation(namespace, "fix/" + CatnipServices.REGISTRIES.getKeyOrThrow(itemLike.asItem())
                                    .getPath())).withItemIngredients(Ingredient.of(itemLike)))
                    .build(c);
        };
        all.add(generatedRecipe);
        return generatedRecipe;
    }

    <T extends ProcessingRecipe<?>> GeneratedRecipe create(Supplier<ItemLike> singleIngredient,
                                                           UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
        return create(CreateNuclear.MOD_ID, singleIngredient, transform);
    }
}
