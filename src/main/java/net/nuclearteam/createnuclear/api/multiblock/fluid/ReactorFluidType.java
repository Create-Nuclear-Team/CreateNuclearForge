package net.nuclearteam.createnuclear.api.multiblock.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder.Reference;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.api.CreateNuclearRegistries;
import net.nuclearteam.createnuclear.api.ReactorFluidTypesValue;
import net.nuclearteam.createnuclear.content.multiblock.fluid.CNReactorFluidTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a reactor fluid type used by the mod's multiblock.
 * <p>
 * A {@code ReactorFluidType} holds a set of fluids that identify this
 * fluid type, heat-related and efficiency values, and a flag indicating
 * whether those values should be read from configuration at runtime.
 */
public record ReactorFluidType(HolderSet<Fluid> fluids, int maxHeat, int efficiency, boolean useConfig) {
    public ReactorFluidType(HolderSet<Fluid> fluids, int maxHeat, int efficiency) {
        this(fluids, maxHeat, efficiency, false);
    }

    /** Serialization codec for saving/loading {@link ReactorFluidType} instances. */
    public static final Codec<ReactorFluidType> CODEC = RecordCodecBuilder.create(i -> i.group(
            RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluids").forGetter(ReactorFluidType::fluids),
            Codec.INT.fieldOf("maxHeat").forGetter(ReactorFluidType::maxHeat),
            Codec.INT.fieldOf("efficiency").forGetter(ReactorFluidType::efficiency)
    ).apply(i, ReactorFluidType::new));

    /**
     * Searches the {@code FLUID_TYPE} registry for a {@link ReactorFluidType}
     * matching the provided {@link Fluid}.
     *
     * @param registryAccess    registry access for the world
     * @param fluid             the fluid to look up
     * @return an {@link Optional} containing a {@link Reference} to the matching
     *         {@code ReactorFluidType} if found, otherwise {@link Optional#empty()}
     */
    public static Optional<Reference<ReactorFluidType>> getTypeForFluid(RegistryAccess registryAccess, Fluid fluid) {
        return registryAccess.lookupOrThrow(CreateNuclearRegistries.FLUID_TYPE)
                .listElements()
                .filter(ref -> ref.value().fluids.stream()
                    .anyMatch(h -> h.unwrapKey()
                        .map(k -> Optional.ofNullable(ForgeRegistries.FLUIDS.getKey(fluid)).map(k.location()::equals).orElse(false))
                        .orElse(false)))
                .findFirst();
    }

    /**
     * Resolves the {@code ReactorFluidType} for a given {@link Fluid}.
     * <p>
     * The resolution first checks the world's registries, then falls back to
     * {@link ReactorFluidTypesValue}, and finally returns the registry fallback
     * value if none is found.
     *
     * @param fluid the fluid whose reactor type should be resolved
     * @param world the level used to access registries
     * @return the resolved {@code ReactorFluidType} (never {@code null})
     */
    public static ReactorFluidType resolveReactorFluidType(Fluid fluid, Level world) {
        return ReactorFluidType.getTypeForFluid(world.registryAccess(), fluid)
                .map(Holder.Reference::value)
                .orElseGet(() -> {
                    ReactorFluidType fromFluid = ReactorFluidTypesValue.getReactorFluidType(fluid);
                    return fromFluid.isNotEmptyFluid()
                            ? fromFluid
                            : world.registryAccess()
                                .registryOrThrow(CreateNuclearRegistries.FLUID_TYPE)
                                .getHolderOrThrow(CNReactorFluidTypes.FALLBACK)
                                .value();
                });
    }


    /**
     * Returns whether this {@code ReactorFluidType} has at least one associated fluid.
     *
     * @return {@code true} if one or more fluids are defined, {@code false} otherwise
     */
    public boolean isNotEmptyFluid() {
        return this.fluids.size() >= 1;
    }

    /**
     * Fluent builder for creating an immutable {@link ReactorFluidType} instance.
     * <p>
     * Use the configuration methods and class {@link #build()} to obtain the
     * resulting instance.
     */
    public static class Builder {
        private final List<Holder<Fluid>> fluids = new ArrayList<>();
        private int maxHeat = 0;
        private int efficiency = 0;
        private boolean useConfig = false;

        private boolean fluidSet = false;
        private boolean maxHeatSet = false;
        private boolean efficiencySet = false;

        /**
         * Sets the maximum heat value for the built {@link ReactorFluidType}.
         *
         * @param maxHeat maximum heat value
         * @return this builder
         */
        public Builder maxHeat(int maxHeat) {
            this.maxHeat = maxHeat;
            this.maxHeatSet = true;
            return this;
        }

        /**
         * Sets the efficiency value for the built {@link ReactorFluidType}.
         *
         * @param efficiency efficiency value
         * @return this builder
         */
        public Builder efficiency(int efficiency) {
            this.efficiency = efficiency;
            this.efficiencySet = true;
            return this;
        }

        /**
         * Adds a fluid to this type from a {@link FluidStack}.
         *
         * @param fluidStack the fluid stack providing the fluid
         * @return this builder
         */
        public Builder fluid(FluidStack fluidStack) {
            this.fluids.add(fluidStack.getFluid().builtInRegistryHolder());
            this.fluidSet = true;
            return this;
        }

        /**
         * Adds a fluid to this type directly.
         *
         * @param fluid the fluid to add
         * @return this builder
         */
        public Builder fluid(Fluid fluid) {
            this.fluids.add(fluid.builtInRegistryHolder());
            this.fluidSet = true;
            return this;
        }

        /**
         * Marks this type to resolve numeric values from configuration at runtime.
         *
         * @return this builder
         */
        public Builder setRodConfig() {
            this.useConfig = true;
            return this;
        }

        /**
         * Builds the immutable {@link ReactorFluidType} instance.
         *
         * @throws IllegalStateException if required fields are missing
         * @return the created instance
         */
        public ReactorFluidType build() {
            List<String> missing = new ArrayList<>();
            if (!fluidSet || fluids.isEmpty()) missing.add("fluids");

            if (!this.useConfig) {
                if (!maxHeatSet) missing.add("maxHeat");
                if (!efficiencySet) missing.add("efficiency");
            }

            if (!missing.isEmpty())
                throw new IllegalStateException("Missing required RodType fields: " + String.join(", ", missing));

            return new ReactorFluidType(HolderSet.direct(fluids), maxHeat, efficiency, useConfig);
        }
    }

    @Override
    public int maxHeat() {
        if (!useConfig) return maxHeat;
        try {
            return 12; //CNConfigs.server().rods.maxHeat.get();
        } catch (IllegalStateException e) {
            return maxHeat;
        }
    }

    @Override
    public int efficiency() {
        if (!useConfig) return efficiency;
        try {
            return 12; //CNConfigs.server().rods.maxHeat.get();
        } catch (IllegalStateException e) {
            return efficiency;
        }
    }
}
