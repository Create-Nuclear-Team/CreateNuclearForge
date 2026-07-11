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
import net.nuclearteam.createnuclear.api.CreateNuclearRegistries;
import net.nuclearteam.createnuclear.api.ItemRodTypesValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a rod type used by the mod's multiblock.
 * <p>
 * A {@code RodType} holds a set of items that can represent this rod type,
 * heat-related values, a timing value, and a {@link TypeRod} indicating the
 * category (fuel, cooler, or mixed).
 */
@MethodsReturnNonnullByDefault
public record RodType(HolderSet<Item> items,
                      Supplier<Integer> baseRodHeat,
                      Supplier<Float> proximityRodHeat,
                      Supplier<Integer> rodTimer,
                      Supplier<Integer> heatRatio,
                      TypeRod type) {

    public RodType(HolderSet<Item> items,
                   int baseRodHeat, float proximityRodHeat,
                   int rodTimer, TypeRod type) {
        this(items, baseRodHeat, proximityRodHeat, rodTimer, type, 1);
    }

    public RodType(HolderSet<Item> items,
                   int baseRodHeat, float proximityRodHeat,
                   int rodTimer, TypeRod type, int heatRatio) {
        this(items, () -> baseRodHeat, () -> proximityRodHeat, () -> rodTimer, () -> heatRatio, type);
    }

    /**
     * Serialization codec for saving/loading {@link RodType} instances.
     */
    public static final Codec<RodType> CODEC = RecordCodecBuilder.create(i -> i.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(RodType::items),
            Codec.INT.fieldOf("baseRodHeat").forGetter(rt -> rt.baseRodHeat().get()),
            Codec.FLOAT.fieldOf("proximityRodHeat").forGetter(rt -> rt.proximityRodHeat().get()),
            Codec.INT.fieldOf("rodTimer").forGetter(rt -> rt.rodTimer().get()),
            StringRepresentable.fromEnum(TypeRod::values).fieldOf("type").forGetter(RodType::type),
            Codec.INT.optionalFieldOf("heatRatio", 1).forGetter(rt -> rt.heatRatio().get())
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
                        .getHolderOrThrow(CreateNuclearRegistries.FALLBACK_ROD)
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
     * Every heat/timing value ({@code baseRodHeat}, {@code proximityRodHeat},
     * {@code rodTimer}, {@code heatRatio}) can be set either as a fixed
     * primitive (evaluated once, at build time) or as a {@link Supplier}
     * (re-evaluated on every call to the corresponding {@link RodType}
     * accessor). The primitive overloads are pure convenience: they simply
     * wrap the given value in a constant supplier, e.g.
     * {@code baseRodHeat(5)} is equivalent to {@code baseRodHeat(() -> 5)}.
     * Use the {@link Supplier} overloads to back a value with a live source
     * such as a config option (see {@code CNItems} for examples), so that
     * changes are picked up without rebuilding the {@code RodType}.
     * <p>
     * {@code items}, {@code type}, {@code baseRodHeat}, {@code proximityRodHeat}
     * and {@code rodTimer} are required; {@code heatRatio} defaults to a
     * constant {@code 1} and never needs to be set explicitly. Call
     * {@link #build()} once all required values are configured.
     */
    public static class Builder {
        private final List<Holder<Item>> items = new ArrayList<>();
        private Supplier<Integer> baseRodHeat = null;
        private Supplier<Float> proximityRodHeat = null;
        private Supplier<Integer> rodTimer = null;
        private Supplier<Integer> heatRatio = () -> 1;
        private TypeRod type = TypeRod.NONE;

        /**
         * Sets a fixed base heat value, evaluated once at build time.
         * Equivalent to {@code baseRodHeat(() -> baseRodHeat)}.
         *
         * @param baseRodHeat constant base heat value
         * @return this builder
         */
        public Builder baseRodHeat(int baseRodHeat) {
            return baseRodHeat(() -> baseRodHeat);
        }

        /**
         * Sets a dynamic base heat value, re-evaluated every time
         * {@link RodType#baseRodHeat()} is called (e.g. to reflect a live
         * config option instead of a value frozen at registration time).
         *
         * @param baseRodHeat supplier producing the base heat value on demand
         * @return this builder
         */
        public Builder baseRodHeat(Supplier<Integer> baseRodHeat) {
            this.baseRodHeat = baseRodHeat;
            return this;
        }

        /**
         * Sets a fixed proximity heat value (heat contributed by nearby rods),
         * evaluated once at build time.
         * Equivalent to {@code proximityRodHeat(() -> proximityRodHeat)}.
         *
         * @param proximityRodHeat constant proximity heat value
         * @return this builder
         */
        public Builder proximityRodHeat(float proximityRodHeat) {
            return proximityRodHeat(() -> proximityRodHeat)  ;
        }

        /**
         * Sets a dynamic proximity heat value, re-evaluated every time
         * {@link RodType#proximityRodHeat()} is called.
         *
         * @param proximityRodHeat supplier producing the proximity heat value on demand
         * @return this builder
         */
        public Builder proximityRodHeat(Supplier<Float> proximityRodHeat) {
            this.proximityRodHeat = proximityRodHeat;
            return this;
        }

        /**
         * Sets a fixed timer (duration) for the rod's behavior, evaluated
         * once at build time. Equivalent to {@code rodTimer(() -> rodTimer)}.
         *
         * @param rodTimer constant duration in ticks or mod-specific units
         * @return this builder
         */
        public Builder rodTimer(int rodTimer) {
            return rodTimer(() -> rodTimer);
        }

        /**
         * Sets a dynamic timer (duration) for the rod's behavior,
         * re-evaluated every time {@link RodType#rodTimer()} is called.
         *
         * @param rodTimer supplier producing the duration on demand
         * @return this builder
         */
        public Builder rodTimer(Supplier<Integer> rodTimer) {
            this.rodTimer = rodTimer;
            return this;
        }

        /**
         * Sets a fixed heat ratio, evaluated once at build time. Optional:
         * defaults to a constant {@code 1} if never called.
         * Equivalent to {@code heatRatio(() -> heatRatio)}.
         *
         * @param heatRatio constant heat ratio value
         * @return this builder
         */
        public Builder heatRatio(int heatRatio) {
            return heatRatio(() -> heatRatio);
        }

        /**
         * Sets a dynamic heat ratio, re-evaluated every time
         * {@link RodType#heatRatio()} is called. Optional: defaults to a
         * constant {@code 1} if never called.
         *
         * @param heatRatio supplier producing the heat ratio on demand
         * @return this builder
         */
        public Builder heatRatio(Supplier<Integer> heatRatio) {
            this.heatRatio = heatRatio;
            return this;
        }

        /**
         * Sets the rod category directly. Prefer the {@link #coolerRodType()}
         * and {@link #fuelRodType()} convenience methods for the common
         * cases; use this overload only to set {@link TypeRod#NONE}
         * explicitly (e.g. for a fallback/sentinel rod type).
         *
         * @param type the rod category to assign
         * @return this builder
         */
        public Builder type(TypeRod type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the rod type to {@link TypeRod#COOLER}.
         *
         * @return this builder
         */
        public Builder coolerRodType() {
            return type(TypeRod.COOLER);
        }

        /**
         * Sets the rod type to {@link TypeRod#FUEL}.
         *
         * @return this builder
         */
        public Builder fuelRodType() {
            return type(TypeRod.FUEL);
        }

        /**
         * Adds one or more items that represent this rod type. Can be called
         * multiple times; items accumulate rather than replace one another.
         * At least one item must be added before {@link #build()}.
         *
         * @param items array of {@link ItemLike} elements to associate
         * @return this builder
         */
        public Builder addItems(ItemLike... items) {
            for (ItemLike provider : items)
                this.items.add(provider.asItem().builtInRegistryHolder());
            return this;
        }

        /**
         * Builds the immutable {@link RodType} instance.
         * <p>
         * Validates that {@code items} is non-empty and that {@code type},
         * {@code baseRodHeat}, {@code proximityRodHeat} and {@code rodTimer}
         * have all been set (either as a fixed value or as a dynamic
         * {@link Supplier}); {@code heatRatio} is exempt since it defaults
         * to a constant {@code 1}.
         *
         * @throws IllegalStateException if one or more required fields are missing,
         *         naming every missing field in the exception message
         * @return the created instance
         */
        public RodType build() {
            List<String> missing = new ArrayList<>();
            if (items.isEmpty()) missing.add("items");
            if (type == null) missing.add("type");
            if (baseRodHeat == null) missing.add("baseRodHeat");
            if (proximityRodHeat == null) missing.add("proximityRodHeat");
            if (rodTimer == null) missing.add("rodTimer");

            if (!missing.isEmpty())
                throw new IllegalStateException("Missing required RodType fields: " + String.join(", ", missing));

            return new RodType(HolderSet.direct(items), baseRodHeat, proximityRodHeat, rodTimer, heatRatio, type);
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
        COOLER,
        /**
         * Sentinel value for the fallback/default rod type — no real category.
         */
        NONE
        ;

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    public static final class TypeRodPredicate {
        public static final Predicate<ItemStack> IS_NOT_NULL = Objects::nonNull;

        public static boolean isFuel(ItemStack stack, Level level) {
            return IS_NOT_NULL.test(stack) && RodType.resolveRodType(stack.getItem(), level).type == TypeRod.FUEL;
        }

        public static boolean isCooled(ItemStack stack, Level level) {
            return IS_NOT_NULL.test(stack) && RodType.resolveRodType(stack.getItem(), level).type == TypeRod.COOLER;
        }

        public static String tooltipKey(ItemStack stack, Level level) {
            if (!IS_NOT_NULL.test(stack)) return "unknown";
            if (isFuel(stack, level)) return "fuel";
            if (isCooled(stack, level)) return "cooled";
            return "unknown";
        }
    }

    @Override
    public String toString() {
        return "RodType [items: " + this.items() +
                ", baseRodHeat: " + this.baseRodHeat().get() +
                ", proximityRodHeat: " + this.proximityRodHeat().get() +
                ", rodTimer: " + this.rodTimer().get() +
                ", heatRatio: " + this.heatRatio().get() +
                ", type: " + this.type() + "]";
    }
}
