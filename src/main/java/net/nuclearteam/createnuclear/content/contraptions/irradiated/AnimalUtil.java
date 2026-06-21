package net.nuclearteam.createnuclear.content.contraptions.irradiated;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.nuclearteam.createnuclear.CNItems;

import java.util.function.Predicate;

public class AnimalUtil {
    private AnimalUtil() {}

    public static boolean isFood(ItemStack stack, Ingredient foodItems) {
        return isFood(stack, foodItems, $ -> true);
    }

    public static boolean isFood(ItemStack stack, Ingredient foodItems, Predicate<ItemStack> extraTest) {
        if (stack.is(CNItems.YELLOWCAKE.get())) {
            return true;
        }

        return foodItems.test(stack) || extraTest.test(stack);
    }
}