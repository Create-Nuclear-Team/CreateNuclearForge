package net.nuclearteam.createnuclear.foundation.data.recipe;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.foundation.data.recipe.ProcessingRecipeGen;
import net.minecraft.data.PackOutput;
import net.nuclearteam.createnuclear.CNFluids;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CreateNuclear;

public class CNCompactingRecipeGen extends CNProcessingRecipeGen {
    GeneratedRecipe
            YELLOWCAKE = create(CreateNuclear.asResource("uranium_fluid_to_yellowcake"), b -> b.require(CNFluids.URANIUM.get(), 100).output(CNItems.YELLOWCAKE, 1));


    public CNCompactingRecipeGen(PackOutput output) {
        super(output);
    }

    @Override
    protected AllRecipeTypes getRecipeType() {
        return AllRecipeTypes.COMPACTING;
    }

}
