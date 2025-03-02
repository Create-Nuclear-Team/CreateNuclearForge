package net.nuclearteam.createnuclear.infrastructure.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers.AddFeaturesBiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import net.nuclearteam.createnuclear.CreateNuclear;

public class CNBiomeModifiers {
    public static final ResourceKey<BiomeModifier>
        URANIUM_ORE = key("uranium_ore"),
        LEAD_ORE = key("lead_ore")
    ;

    private static ResourceKey<BiomeModifier> key(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, CreateNuclear.asResource(name));
    }

    public static void bootstrap(BootstapContext<BiomeModifier> ctx) {
        HolderGetter<Biome> biomeLookup = ctx.lookup(Registries.BIOME);
        HolderSet<Biome> isOverworld = biomeLookup.getOrThrow(BiomeTags.IS_OVERWORLD);

        HolderGetter<PlacedFeature> featureLookup = ctx.lookup(Registries.PLACED_FEATURE);
        Holder<PlacedFeature> uraniumOre = featureLookup.getOrThrow(CNPlacedFeatures.URANIUM_ORE);
        Holder<PlacedFeature> leadOre = featureLookup.getOrThrow(CNPlacedFeatures.LEAD_ORE);

        ctx.register(URANIUM_ORE, addOre(isOverworld, uraniumOre));
        ctx.register(LEAD_ORE, addOre(isOverworld, leadOre));
    }

    private static AddFeaturesBiomeModifier addOre(HolderSet<Biome> biomes, Holder<PlacedFeature> feature) {
        return new AddFeaturesBiomeModifier(biomes, HolderSet.direct(feature), GenerationStep.Decoration.UNDERGROUND_ORES);
    }
}
