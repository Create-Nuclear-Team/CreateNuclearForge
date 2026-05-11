package net.nuclearteam.createnuclear.infrastructure.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.nuclearteam.createnuclear.CNEntityType;
import net.nuclearteam.createnuclear.content.particles.IrradiatedParticlesData;

public class IrradiatedBiomes {
    public static Biome createPlain(HolderGetter<PlacedFeature> featureLookup, HolderGetter<ConfiguredWorldCarver<?>> carverLookup) {
        return IrradiatedBiomes.irradiated(featureLookup, carverLookup, new BiomeGenerationSettings.Builder(featureLookup, carverLookup));
    }

    public static void monsters(MobSpawnSettings.Builder builder, int zombieWeight, int zombieVillagerWeight, int skeletonWeight) {

        // builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(CNEntityType.IRRADIATED_CAT.get(), 100, 2, 4));
        // builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(CNEntityType.IRRADIATED_CHICKEN.get(), 100, 1, 4));
        // builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(CNEntityType.IRRADIATED_WOLF.get(), 100, 2, 4));
    }



    public static void addDefaultIrradiatedOres(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, MiscOverworldPlacements.BLUE_ICE);
        builder.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
    }

    public static void addDefaultSoftDisks(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.VOID_START_PLATFORM);
    }

    public static Biome irradiated(HolderGetter<PlacedFeature> featureGetter, HolderGetter<ConfiguredWorldCarver<?>> carverGetter, BiomeGenerationSettings.Builder generation) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        BiomeSpecialEffects.Builder effectBuilder = new BiomeSpecialEffects.Builder();

        effectBuilder
                // Eau : Verte croupie
                .waterColor(0x3B5133)
                .waterFogColor(0x0A0E0A)

                // BROUILLARD : Vert radioactif maladif (plus vif pour être visible)
                .fogColor(0x485E3E)

                // CIEL : Gris-vert délavé (permet de voir le soleil/lune et le cycle jour/nuit)
                .skyColor(0x324132)

                // HERBE : Jaune "soufre" / Brûlée
                .grassColorOverride(0x8C8F5B)

                // FEUILLES : Olive desséché
                .foliageColorOverride(0x565E3E)

                .ambientParticle(new AmbientParticleSettings(new IrradiatedParticlesData(), 0.025F))
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS);

        IrradiatedBiomes.addDefaultIrradiatedOres(generation);
        IrradiatedBiomes.addDefaultSoftDisks(generation);
        IrradiatedBiomes.monsters(spawnBuilder, 95, 5, 100);

        return new Biome.BiomeBuilder()
                .mobSpawnSettings(spawnBuilder.build())
                .hasPrecipitation(false)
                .temperature(2.0f)
                .downfall(0f)
                .specialEffects(effectBuilder.build())
                .generationSettings(generation.build())
                .build();
    }
}