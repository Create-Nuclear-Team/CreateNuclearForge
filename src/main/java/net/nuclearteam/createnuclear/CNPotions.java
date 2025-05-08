package net.nuclearteam.createnuclear;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class CNPotions {

    public static final DeferredRegister<Potion> CNPOTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, CreateNuclear.MOD_ID);

    public static final RegistryObject<Potion> POTION_1 = CNPOTIONS.register("potion_of_radiation_1",
            () -> new Potion(new MobEffectInstance(CNEffects.RADIATION.get(), 900)));
    public static final RegistryObject<Potion> POTION_AUGMENT_1 = CNPOTIONS.register("potion_of_radiation_augment_1",
            () -> new Potion(new MobEffectInstance(CNEffects.RADIATION.get(), 1800)));
    public static final RegistryObject<Potion> POTION_2 = CNPOTIONS.register("potion_of_radiation_2",
            () -> new Potion(new MobEffectInstance(CNEffects.RADIATION.get(), 410, 1)));

    public static void register(IEventBus eventBus) {
        CNPOTIONS.register(eventBus);
    }

    public static void registerPotionsRecipes() {
        PotionBrewing.addMix(Potions.AWKWARD, CNItems.ENRICHED_YELLOWCAKE.get(), POTION_1.get());
        PotionBrewing.addMix(POTION_1.get(), Items.REDSTONE, POTION_AUGMENT_1.get());
        PotionBrewing.addMix(POTION_1.get(), Items.GLOWSTONE_DUST, POTION_2.get());
    }
}
