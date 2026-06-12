package net.nuclearteam.createnuclear.foundation.data.recipe;

import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;
import net.nuclearteam.createnuclear.CNFluids;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.CreateNuclear;


public class CNMixingRecipeGen extends MixingRecipeGen {

    GeneratedRecipe
        STEEL = create("steel", b -> b
            .require(CNTags.forgeItemTag("dusts/coal"))
            .require(Tags.Items.INGOTS_IRON)
            .output(CNItems.STEEL_INGOT)
        ),

        URANIUM_FLUID = create("uranium_fluid", b -> b
            .require(CNTags.forgeItemTag("dusts/uranium"))
            .output(CNFluids.URANIUM.get(), 25)

        ),

        THORIUM_FLUID = create("thorium_fluid", b -> b
            .require(CNTags.forgeItemTag("dusts/thorium"))
            .output(CNFluids.THORIUM.get(), 25)
            .requiresHeat(HeatCondition.HEATED)
        ),

        NITROGEN = create("liquid_nitrogen", b -> b
                .require(CNItems.COOLED_NITROGEN_CONCENTRATE)
                .duration(20)
                .require(Items.ICE)
                .output(CNFluids.LIQUID_NITROGEN.get(), 100)
        );
    ;

    public CNMixingRecipeGen(PackOutput generator) {
        super(generator, CreateNuclear.MOD_ID);
    }
}
