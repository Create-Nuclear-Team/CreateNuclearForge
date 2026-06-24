package net.nuclearteam.createnuclear.infrastructure.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.nuclearteam.createnuclear.CreateNuclear;
import org.jetbrains.annotations.NotNull;

public class CNBiomes {
    public static final class Irradiated {
        public static final ResourceKey<Biome> PLAIN = key("irradiated_land");
    }

    public static void bootstrapRegistries(BootstapContext<Biome> context) {
        HolderGetter<PlacedFeature> featureLookup = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carverLookup = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(Irradiated.PLAIN, IrradiatedBiomes.createPlain(featureLookup, carverLookup));
    }

    public static @NotNull ResourceKey<Biome> key(String id) {
        return ResourceKey.create(Registries.BIOME, CreateNuclear.asResource(id));
    }
}
