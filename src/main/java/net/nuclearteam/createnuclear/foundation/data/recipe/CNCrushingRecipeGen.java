package net.nuclearteam.createnuclear.foundation.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.api.data.recipe.CrushingRecipeGen;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class CNCrushingRecipeGen extends CrushingRecipeGen {

    GeneratedRecipe
        COAL_DUST = create("coal", b -> b
            .duration(250)
            .require(ItemTags.COALS)
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

        RAW_URANIUM_BLOCK = create(() -> CNBlocks.RAW_URANIUM_BLOCK, b -> b
            .duration(250)
            .output(1, CNItems.URANIUM_POWDER,81)
        ),

        RAW_ZINC = create(() -> AllItems.RAW_ZINC, b -> b.duration(250)
            .output(1, AllItems.CRUSHED_ZINC, 1)
            .output(.75f, AllItems.EXP_NUGGET, 1)
            .output(.25f, CNItems.LEAD_NUGGET,1)
        ),


        RAW_COPPER = create(() -> Items.RAW_COPPER, b -> b.duration(250)
            .output(1, AllItems.CRUSHED_COPPER, 1)
            .output(.75f, AllItems.EXP_NUGGET, 1)
            .output(.15f, CNItems.LEAD_NUGGET,1)
        )
    ;

    public CNCrushingRecipeGen(PackOutput generator) {
        super(generator, CreateNuclear.MOD_ID);
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
}
