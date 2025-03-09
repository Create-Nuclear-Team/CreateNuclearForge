package net.nuclearteam.createnuclear.infrastructure.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.core.RegistrySetBuilder.RegistryBootstrap;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.nuclearteam.createnuclear.CNDamageTypes;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.infrastructure.worldgen.CNBiomeModifiers;
import net.nuclearteam.createnuclear.infrastructure.worldgen.CNCOnfiguredFeatures;
import net.nuclearteam.createnuclear.infrastructure.worldgen.CNPlacedFeatures;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class GeneratedEntriesProvider extends DatapackBuiltinEntriesProvider {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, CNDamageTypes::bootstrap)
            .add(Registries.CONFIGURED_FEATURE, (RegistryBootstrap) CNCOnfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, CNPlacedFeatures::bootstrap)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, CNBiomeModifiers::bootstrap)
            ;

    public GeneratedEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(CreateNuclear.MOD_ID));
    }

    @Override
    public String getName() {
        return "CreateNuclear Generated Registry Entries";
    }
}
