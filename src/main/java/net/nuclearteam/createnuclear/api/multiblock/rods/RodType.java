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
import net.minecraft.world.level.ItemLike;
import net.nuclearteam.createnuclear.api.CreateNuclearRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
public record RodType(HolderSet<Item> items,
                      int baseRodHeat, int proximityRodHeat,
                      int rodTimer, TypeRod type) {
    public static final Codec<RodType> CODEC = RecordCodecBuilder.create(i -> i.group(
        RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items", HolderSet.direct()).forGetter(RodType::items),
        Codec.INT.optionalFieldOf("baseRodHeat", 0).forGetter(RodType::baseRodHeat),
        Codec.INT.optionalFieldOf("proximityRodHeat", 0).forGetter(RodType::proximityRodHeat),
        Codec.INT.optionalFieldOf("rodTimer", 0).forGetter(RodType::rodTimer),
        StringRepresentable.fromEnum(TypeRod::values).optionalFieldOf("type", TypeRod.MIXTE).forGetter(RodType::type)
    ).apply(i, RodType::new));

    public static Optional<Reference<RodType>> getTypeForItem(RegistryAccess registryAccess, Item item) {
        return registryAccess.lookupOrThrow(CreateNuclearRegistries.ROD_TYPE)
            .listElements()
            .filter(ref -> ref.value().items.contains(item.builtInRegistryHolder()))
            .findFirst();
    }

    public boolean isEmptyItem() {
        return this.items.size() < 1;
    }

    public static class Builder {
        private final List<Holder<Item>> items = new ArrayList<>();
        private int baseRodHeat = 0;
        private int proximityRodHeat = 0;
        private int rodTimer = 0;
        private TypeRod type = TypeRod.MIXTE;

        private boolean itemsSet = false;
        private boolean baseRodHeatSet = false;
        private boolean proximityRodHeatSet = false;
        private boolean rodTimerSet = false;
        private boolean typeSet = false;

        public Builder baseRodHeat(int baseRodHeat) {
            this.baseRodHeat = baseRodHeat;
            this.baseRodHeatSet = true;
            return this;
        }

        public Builder proximityRodHeat(int proximityRodHeat) {
            this.proximityRodHeat = proximityRodHeat;
            this.proximityRodHeatSet = true;
            return this;
        }

        public Builder rodTimer(int rodTimer) {
            this.rodTimer = rodTimer;
            this.rodTimerSet = true;
            return this;
        }

        public Builder coolerRodType() {
            this.type = TypeRod.COOLER;
            this.typeSet = true;
            return this;
        }

        public Builder fuelRodType() {
            this.type = TypeRod.FUEL;
            this.typeSet = true;
            return this;
        }

        public Builder mixteRodType() {
            this.type = TypeRod.MIXTE;
            this.typeSet = true;
            return this;
        }

        public Builder addItems(ItemLike... items) {
            for (ItemLike provider : items)
                this.items.add(provider.asItem().builtInRegistryHolder());
            if (items.length > 0) this.itemsSet = true;
            return this;
        }

        public RodType build() {
            List<String> missing = new ArrayList<>();
            if (!itemsSet || items.isEmpty()) missing.add("items");
            if (!baseRodHeatSet) missing.add("baseRodHeat");
            if (!proximityRodHeatSet) missing.add("proximityRodHeat");
            if (!rodTimerSet) missing.add("rodTimer");
            if (!typeSet) missing.add("type");

            if (!missing.isEmpty())
                throw new IllegalStateException("Missing required RodType fields: " + String.join(", ", missing));

            return new RodType(HolderSet.direct(items), baseRodHeat, proximityRodHeat, rodTimer, type);
        }
    }


    public enum TypeRod implements StringRepresentable {
        FUEL,
        COOLER,
        MIXTE;

        @Override
        public String getSerializedName() {
            return name();
        }
    }
}
