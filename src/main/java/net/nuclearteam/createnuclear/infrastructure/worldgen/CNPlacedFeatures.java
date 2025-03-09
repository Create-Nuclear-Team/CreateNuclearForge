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
        LEAD_ORE = key("lead_ore")
    ;

    private static ResourceKey<PlacedFeature> key(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, CreateNuclear.asResource(name));
    }

    public static void bootstrap(BootstapContext<PlacedFeature> ctx) {
        HolderGetter<ConfiguredFeature<?, ?>> featureLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> uraniumOre = featureLookup.getOrThrow(CNCOnfiguredFeatures.URANIUM_ORE);
        Holder<ConfiguredFeature<?, ?>> leadOre = featureLookup.getOrThrow(CNCOnfiguredFeatures.LEAD_ORE);

        register(ctx, URANIUM_ORE, uraniumOre, placement(CountPlacement.of(6), -64, 64));
        register(ctx, LEAD_ORE, leadOre, placement(CountPlacement.of(6), -64, 64));
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
