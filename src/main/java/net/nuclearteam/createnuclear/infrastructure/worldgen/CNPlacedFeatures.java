package net.nuclearteam.createnuclear.infrastructure.worldgen;

import com.simibubi.create.infrastructure.worldgen.ConfigPlacementFilter;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.List;

import static net.minecraft.data.worldgen.placement.PlacementUtils.register;

public class CNPlacedFeatures {
    public static final ResourceKey<PlacedFeature>
        URANIUM_ORE = key("uranium_ore"),
        LEAD_ORE = key("lead_ore"),
        STRIATED_ORES_OVERWORLD = key("striated_ores_overworld")
    ;

    private static ResourceKey<PlacedFeature> key(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, CreateNuclear.asResource(name));
    }

    public static void bootstrap(BootstapContext<PlacedFeature> ctx) {
        HolderGetter<ConfiguredFeature<?, ?>> featureLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> uraniumOre = featureLookup.getOrThrow(CNConfiguredFeatures.URANIUM_ORE);
        Holder<ConfiguredFeature<?, ?>> leadOre = featureLookup.getOrThrow(CNConfiguredFeatures.LEAD_ORE);
        Holder<ConfiguredFeature<?, ?>> striatedOresOverworld = featureLookup.getOrThrow(CNConfiguredFeatures.STRIATED_ORES_OVERWORLD);

        register(ctx, URANIUM_ORE, uraniumOre, placementOres(CountPlacement.of(6), -64, 64));
        register(ctx, LEAD_ORE, leadOre, placementOres(CountPlacement.of(6), -64, 64));
        register(ctx, STRIATED_ORES_OVERWORLD, striatedOresOverworld, placement(RarityFilter.onAverageOnceEvery(18), -30, 70));

    }

    private static List<PlacementModifier> placementOres(PlacementModifier frequency, int minHeight, int maxHeight) {
        return List.of(
                frequency,
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(minHeight), VerticalAnchor.absolute(maxHeight)),
                ConfigPlacementFilter.INSTANCE
        );
    }

    private static List<PlacementModifier> placement(PlacementModifier frequency, int minHeight, int maxHeight) {
        return List.of(
                frequency,
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(minHeight), VerticalAnchor.absolute(maxHeight)),
                ConfigPlacementFilter.INSTANCE
        );
    }
}
