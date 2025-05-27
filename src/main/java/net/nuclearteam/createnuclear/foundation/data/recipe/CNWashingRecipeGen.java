package net.nuclearteam.createnuclear.foundation.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.foundation.data.recipe.CompatMetals;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.nuclearteam.createnuclear.CNItems;

import java.util.function.Supplier;

public class CNWashingRecipeGen extends CNProcessingRecipeGen {

    GeneratedRecipe
        CRUSHED_LEAD = crushedOre(AllItems.CRUSHED_LEAD, CNItems.LEAD_NUGGET::get, AllItems.EXP_NUGGET::get, .5f)
    ;

    public GeneratedRecipe convert(Block block, Block result) {
        return create(() -> block, b -> b.output(result));
    }

    public GeneratedRecipe crushedOre(ItemEntry<? extends Item> crushed, Supplier<ItemLike> nugget, Supplier<ItemLike> secondary,
                                      float secondaryChance) {
        return create(crushed::get, b -> b.output(nugget.get(), 9)
                .output(secondaryChance, secondary.get(), 1));
    }

    public GeneratedRecipe moddedCrushedOre(ItemEntry<? extends Item> crushed, CompatMetals metal) {
        for (Mods mod : metal.getMods()) {
            String metalName = metal.getName(mod);
            ResourceLocation nugget = mod.nuggetOf(metalName);
            create(mod.getId() + "/" + crushed.getId()
                            .getPath(),
                    b -> b.withItemIngredients(Ingredient.of(crushed::get))
                            .output(1, nugget, 9)
                            .whenModLoaded(mod.getId()));
        }
        return null;
    }

    public GeneratedRecipe simpleModded(Mods mod, String input, String output) {
        return create(mod.getId() + "/" + output, b -> b.require(mod, input)
                .output(mod, output).whenModLoaded(mod.getId()));
    }

    public CNWashingRecipeGen(PackOutput output) {
        super(output);
    }

    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return AllRecipeTypes.SPLASHING;
    }
}
