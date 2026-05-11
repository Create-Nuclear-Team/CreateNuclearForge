package net.nuclearteam.createnuclear.api.multiblock.rods;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.nuclearteam.createnuclear.CNTags.CNItemTags;
import net.nuclearteam.createnuclear.api.CreateNuclearRegistries;
import net.nuclearteam.createnuclear.api.ItemRodTypesValue;
import net.nuclearteam.createnuclear.content.multiblock.rod.CNRodTypes;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represents a rod type used by the mod's multiblock.
 * <p>
 * A {@code RodType} holds a set of items that can represent this rod type,
 * heat-related values, a timing value, and a {@link TypeRod} indicating the
 * category (fuel, cooler, or mixed).
 */
@MethodsReturnNonnullByDefault
public record RodType(HolderSet<Item> items,
                      int baseRodHeat, int proximityRodHeat,
                      int rodTimer, TypeRod type, boolean useConfig) {

    public RodType(HolderSet<Item> items,
                   int baseRodHeat, int proximityRodHeat,
                   int rodTimer, TypeRod type) {
        this(items, baseRodHeat, proximityRodHeat, rodTimer, type, false);
    }

    /**
     * Serialization codec for saving/loading {@link RodType} instances.
     */
    public static final Codec<RodType> CODEC = RecordCodecBuilder.create(i -> i.group(
        RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(RodType::items),
        Codec.INT.fieldOf("baseRodHeat").forGetter(RodType::baseRodHeat),
        Codec.INT.fieldOf("proximityRodHeat").forGetter(RodType::proximityRodHeat),
        Codec.INT.fieldOf("rodTimer").forGetter(RodType::rodTimer),
        StringRepresentable.fromEnum(TypeRod::values).fieldOf("type").forGetter(RodType::type)
    ).apply(i, RodType::new));

    /**
     * Searches the {@code ROD_TYPE} registry for a {@link RodType} matching the
     * provided {@link Item}.
     *
     * @param registryAccess registry access for the world
     * @param item           the item to look up
     * @return an {@link Optional} containing a {@link Reference} to the matching
     *         {@code RodType} if found, otherwise {@link Optional#empty()}
     */
    public static Optional<Reference<RodType>> getTypeForItem(RegistryAccess registryAccess, Item item) {
        return registryAccess.lookupOrThrow(CreateNuclearRegistries.ROD_TYPE)
            .listElements()
            .filter(ref -> ref.value().items.contains(item.builtInRegistryHolder()))
            .findFirst();
    }

    /**
     * Resolves the {@code RodType} for a given {@link Item}. The method first
     * checks the world's registries, then falls back to {@link ItemRodTypesValue},
     * and lastly returns a fallback registry value if none is found.
     *
     * @param item  the item whose rod type should be resolved
     * @param world the world (level) used to access registries
     * @return the resolved {@code RodType} (never {@code null})
     */
    public static RodType resolveRodType(Item item, Level world) {
        return RodType.getTypeForItem(world.registryAccess(), item)
            .map(Holder.Reference::value)
            .orElseGet(() -> {
                RodType fromItem = ItemRodTypesValue.getRodType(item);
                return fromItem.isNotEmptyItem()
                    ? fromItem
                    : world.registryAccess()
                        .registryOrThrow(CreateNuclearRegistries.ROD_TYPE)
                        .getHolderOrThrow(CNRodTypes.FALLBACK)
                        .value();
            });
    }

    /**
     * Returns whether this {@code RodType} has no associated items.
     *
     * @return {@code true} if no items are defined, {@code false} otherwise
     */
    public boolean isNotEmptyItem() {
        return this.items.size() >= 1;
    }

    /**
     * Fluent builder for creating an immutable {@link RodType} instance.
     * <p>
     * Use the configuration methods and call {@link #build()} to obtain the
     * resulting instance.
     */
    public static class Builder {
        private final List<Holder<Item>> items = new ArrayList<>();
        private int baseRodHeat = 0;
        private int proximityRodHeat = 0;
        private int rodTimer = 0;
        private TypeRod type = TypeRod.FUEL;
        private boolean useConfig = false;

        private boolean itemsSet = false;
        private boolean baseRodHeatSet = false;
        private boolean proximityRodHeatSet = false;
        private boolean rodTimerSet = false;
        private boolean typeSet = false;

        /**
         * Sets the base heat for the rod.
         *
         * @param baseRodHeat base heat value
         * @return this builder
         */
        public Builder baseRodHeat(int baseRodHeat) {
            this.baseRodHeat = baseRodHeat;
            this.baseRodHeatSet = true;
            return this;
        }

        /**
         * Sets the heat contributed by nearby rods.
         *
         * @param proximityRodHeat proximity heat value
         * @return this builder
         */
        public Builder proximityRodHeat(int proximityRodHeat) {
            this.proximityRodHeat = proximityRodHeat;
            this.proximityRodHeatSet = true;
            return this;
        }

        /**
         * Sets the timer (duration) for the rod's behavior.
         *
         * @param rodTimer duration in ticks or mod-specific units
         * @return this builder
         */
        public Builder rodTimer(int rodTimer) {
            this.rodTimer = rodTimer;
            this.rodTimerSet = true;
            return this;
        }

        /**
         * Sets the rod type to {@link TypeRod#COOLER}.
         *
         * @return this builder
         */
        public Builder coolerRodType() {
            this.type = TypeRod.COOLER;
            this.typeSet = true;
            return this;
        }

        /**
         * Sets the rod type to {@link TypeRod#FUEL}.
         *
         * @return this builder
         */
        public Builder fuelRodType() {
            this.type = TypeRod.FUEL;
            this.typeSet = true;
            return this;
        }

        /**
         * Adds one or more items that represent this rod type.
         *
         * @param items array of {@link ItemLike} elements to associate
         * @return this builder
         */
        public Builder addItems(ItemLike... items) {
            for (ItemLike provider : items)
                this.items.add(provider.asItem().builtInRegistryHolder());
            if (items.length > 0) this.itemsSet = true;
            return this;
        }

        public Builder setRodConfig() {
            this.useConfig = true;
            return this;
        }

        /**
         * Builds the immutable {@link RodType} instance.
         *
         * @throws IllegalStateException if required fields are missing
         * @return the created instance
         */
        public RodType build() {
            List<String> missing = new ArrayList<>();
            if (!itemsSet || items.isEmpty()) missing.add("items");
            if (!typeSet) missing.add("type");

            if (!this.useConfig) {
                if (!baseRodHeatSet) missing.add("baseRodHeat");
                if (!proximityRodHeatSet) missing.add("proximityRodHeat");
                if (!rodTimerSet) missing.add("rodTimer");
            } else {
                /* Lazy config resolution: don't access CNConfigs here during registration.
                 * The actual values will be read at runtime via the resolved* accessors.
                 * Only MIXTE is considered an error for "useConfig" because there are
                 * no configuration defaults for the mixed type.
                 */
//                missing.add("Configuration defaults cannot be applied to the rod type");
            }

            if (!missing.isEmpty())
                throw new IllegalStateException("Missing required RodType fields: " + String.join(", ", missing));

            return new RodType(HolderSet.direct(items), baseRodHeat, proximityRodHeat, rodTimer, type, useConfig);
        }
    }

    @Override
    public int baseRodHeat() {
        if (!useConfig) return baseRodHeat;
        try {
            return switch (type) {
                case FUEL -> CNConfigs.server().rods.baseValueUranium.get();
                case COOLER -> CNConfigs.server().rods.baseValueGraphite.get();
                default -> baseRodHeat;
            };
        } catch (IllegalStateException e) {
            return baseRodHeat;
        }
    }

    @Override
    public int proximityRodHeat() {
        if (!useConfig) return proximityRodHeat;
        try {
            return (int) switch (type) {
                case FUEL -> CNConfigs.server().rods.uraniumProxyBonus.get();
                case COOLER -> Math.round(CNConfigs.server().rods.graphiteProxyMalus.get());
                default -> proximityRodHeat;
            };
        } catch (IllegalStateException e) {
            return proximityRodHeat;
        }
    }

    @Override
    public int rodTimer() {
        if (!useConfig) return rodTimer;
        try {
            return switch (type) {
                case FUEL -> CNConfigs.server().rods.uraniumRodLifetime.get();
                case COOLER -> CNConfigs.server().rods.graphiteRodLifetime.get();
                default -> rodTimer;
            };
        } catch (IllegalStateException e) {
            return rodTimer;
        }
    }


    public enum TypeRod implements StringRepresentable {
        /**
         * Fuel rod: generates heat via reaction.
         */
        FUEL,
        /**
         * Cooler rod: reduces or absorbs heat.
         */
        COOLER
        ;

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    public static final class TypeRodPredicate {
        public static final Predicate<ItemStack> IS_NOT_NULL = Objects::nonNull;

        public static final Predicate<ItemStack> IS_MIXTE = s -> IS_NOT_NULL.test(s) && ItemRodTypesValue.getRodType(s.getItem()).type == TypeRod.MIXTE;

        public static final Predicate<ItemStack> IS_FUEL = s -> {
            TypeRod type = ItemRodTypesValue.getRodType(s.getItem()).type;
            return IS_NOT_NULL.test(s) && (s.is(CNItemTags.FUEL.tag) || type == TypeRod.FUEL);
        };

        public static final Predicate<ItemStack> IS_COOLED = s -> {
            TypeRod type = ItemRodTypesValue.getRodType(s.getItem()).type;
            return IS_NOT_NULL.test(s) && (s.is(CNItemTags.COOLER.tag) || type == TypeRod.COOLER);
        };

        public static String tooltipKey(ItemStack stack) {
            if (!IS_NOT_NULL.test(stack)) return "unknown";
            if (IS_FUEL.or(IS_MIXTE).test(stack)) return "fuel";
            if (IS_COOLED.or(IS_MIXTE).test(stack)) return "cooled";
            return "unknown";
        }
    }
}
