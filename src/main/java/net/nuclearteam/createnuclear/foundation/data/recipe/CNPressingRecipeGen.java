package net.nuclearteam.createnuclear.foundation.data.recipe;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.crafting.Ingredient;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.function.UnaryOperator;

public class CNPressingRecipeGen extends CNProcessingRecipeGen {

    GeneratedRecipe
            GRAPHENE = create("graphene", b -> b
            .require(Ingredient.of(CNTags.forgeItemTag("coal_dusts")))
            .output(CNItems.GRAPHENE)
    )
            ;



    <T extends ProcessingRecipe<?>> GeneratedRecipe create(String name, UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
        return create(CreateNuclear.asResource(name), transform);
    }


    public CNPressingRecipeGen(PackOutput generator) {
        super(generator);
    }

    @Override
    protected AllRecipeTypes getRecipeType() {
        return AllRecipeTypes.PRESSING;
    }
}
