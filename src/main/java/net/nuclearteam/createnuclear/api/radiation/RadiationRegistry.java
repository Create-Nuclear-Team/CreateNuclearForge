package net.nuclearteam.createnuclear.api.radiation;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RadiationRegistry {
    private static final Map<Item, Double> ITEM_VALUE = new HashMap<>();
    private static final Map<TagKey<Item>, Double> TAG_VALUE = new HashMap<>();
    private static final Map<ResourceKey<Biome>, Double> BIOME_VALUE = new HashMap<>();

    private RadiationRegistry() {}

    public static Builder register() {
        return new Builder();
    }

    public static double get(ItemStack stack) {
        Item item = stack.getItem();

        Double value = ITEM_VALUE.get(item);
        if (value != null) return value;

        for (Entry<TagKey<Item>, Double> entry : TAG_VALUE.entrySet()) {
            if (stack.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return 0D;
    }

    public static double get(ResourceKey<Biome> stack) {
        Double value = BIOME_VALUE.get(stack);
        if (value != null) return value;

        return 0D;
    }

    public static double getRadiation(ItemStack stack, Player player) {
        return get(stack) * stack.getCount();
    }
    public static double getRadiation(ResourceKey<Biome> biome, Player player) {
        return get(biome);
    }

    public static class Builder {
        private Item item;
        private TagKey<Item> tag;
        private ResourceKey<Biome> biome;
        private double value;

        public Builder item(Item item) {
            this.item = item;
            return this;
        }

        public Builder tag(TagKey<Item> tag) {
            this.tag = tag;
            return this;
        }

        public Builder biome(ResourceKey<Biome> biome) {
            this.biome = biome;
            return this;
        }

        public Builder value(double value) {
            this.value = value;
            return this;
        }

        public void build() {
            if (item != null) {
                ITEM_VALUE.put(item, value);
            }

            if (tag != null) {
                TAG_VALUE.put(tag, value);
            }

            if (biome != null) {
                BIOME_VALUE.put(biome, value);
            }
        }
    }
}
