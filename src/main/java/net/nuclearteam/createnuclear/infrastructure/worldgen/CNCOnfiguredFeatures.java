package net.nuclearteam.createnuclear.infrastructure.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CreateNuclear;
import static net.minecraft.data.worldgen.features.FeatureUtils.register;

import java.util.List;

public class CNCOnfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>>
        URANIUM_ORE = key("uranium_ore"),
        LEAD_ORE = key("lead_ore")
    ;

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, CreateNuclear.asResource(name));
    }

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> ctx) {
        RuleTest stoneOreReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateOreReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<TargetBlockState> uraniumTargetStates = List.of(
                OreConfiguration.target(stoneOreReplaceables, CNBlocks.URANIUM_ORE.getDefaultState()),
                OreConfiguration.target(deepslateOreReplaceables, CNBlocks.DEEPSLATE_URANIUM_ORE.getDefaultState())
        );

        register(ctx, URANIUM_ORE, Feature.ORE, new OreConfiguration(uraniumTargetStates, 7));

        List<TargetBlockState> leadTargetStates = List.of(
                OreConfiguration.target(stoneOreReplaceables, CNBlocks.LEAD_ORE.getDefaultState()),
                OreConfiguration.target(deepslateOreReplaceables, CNBlocks.DEEPSLATE_LEAD_ORE.getDefaultState())
        );

        register(ctx, LEAD_ORE, Feature.ORE, new OreConfiguration(leadTargetStates, 7));
    }
}
