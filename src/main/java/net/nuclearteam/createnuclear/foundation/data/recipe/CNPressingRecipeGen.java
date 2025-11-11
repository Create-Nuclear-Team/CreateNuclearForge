package net.nuclearteam.createnuclear.foundation.data.recipe;

import com.simibubi.create.api.data.recipe.PressingRecipeGen;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.crafting.Ingredient;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.CreateNuclear;

public class CNPressingRecipeGen extends PressingRecipeGen {

    GeneratedRecipe
        GRAPHENE = create("graphene", b -> b
            .require(Ingredient.of(CNTags.forgeItemTag("coal_dusts")))
            .output(CNItems.GRAPHENE)
    )
    ;

    public CNPressingRecipeGen(PackOutput generator) {
        super(generator, CreateNuclear.MOD_ID);
    }


}
