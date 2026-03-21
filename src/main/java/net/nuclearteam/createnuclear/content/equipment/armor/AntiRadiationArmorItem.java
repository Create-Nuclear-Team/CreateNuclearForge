package net.nuclearteam.createnuclear.content.equipment.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.nuclearteam.createnuclear.CNAttributes;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CNTags.CNItemTags;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
@MethodsReturnNonnullByDefault
public class AntiRadiationArmorItem {

    public static final ArmorItem.Type HELMET = ArmorItem.Type.HELMET;
    public static final ArmorItem.Type CHESTPLATE = ArmorItem.Type.CHESTPLATE;
    public static final ArmorItem.Type LEGGINGS = ArmorItem.Type.LEGGINGS;
    public static final ArmorItem.Type BOOTS = ArmorItem.Type.BOOTS;
    public static final ArmorMaterial ARMOR_MATERIAL = ArmorMaterials.ANTI_RADIATION_SUIT;

    private static final EnumMap<ArmorItem.Type, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266744_) -> {
        p_266744_.put(ArmorItem.Type.BOOTS, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
        p_266744_.put(ArmorItem.Type.LEGGINGS, UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
        p_266744_.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
        p_266744_.put(ArmorItem.Type.HELMET, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
    });

    private static void irradiatedArmorAttribute(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder, ArmorItem.Type type) {
        UUID uuid = ARMOR_MODIFIER_UUID_PER_TYPE.get(type);
        builder.put(CNAttributes.IRRADIATED_RESISTANCE.get(), new AttributeModifier(uuid, "Armor Resistance Irradiation", 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }


    public static void attachCustomModel(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private AntiRadiationArmorModel<LivingEntity> model;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                          EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.model == null) {
                    EntityModelSet models = Minecraft.getInstance().getEntityModels();
                    ModelPart root = models.bakeLayer(AntiRadiationArmorModel.LAYER_LOCATION);
                    this.model = new AntiRadiationArmorModel<>(root);
                }

                // 1. On configure la VISIBILITÉ sur les parties STANDARDS de notre modèle
                // (On cache tout par défaut, puis on active seulement ce qui correspond au slot)
                this.model.setAllVisible(false);
                switch (equipmentSlot) {
                    case HEAD -> this.model.head.visible = true;
                    case CHEST -> {
                        this.model.body.visible = true;
                        this.model.rightArm.visible = true;
                        this.model.leftArm.visible = true;
                    }
                    case LEGS -> {
                        this.model.body.visible = true;
                        this.model.rightLeg.visible = true;
                        this.model.leftLeg.visible = true;
                    }
                    case FEET -> {
                        this.model.rightLeg.visible = true;
                        this.model.leftLeg.visible = true;
                    }
                }

                // 2. On vole les ANIMATIONS du modèle original (qui fonctionne déjà)
                if (original instanceof HumanoidModel) {
                    HumanoidModel<LivingEntity> castedOriginal = (HumanoidModel<LivingEntity>) original;


                    // Copie des états (accroupi, bébé, chevauchement...)
                    castedOriginal.copyPropertiesTo(this.model);

                    // COPIE DIRECTE DES ROTATIONS (La solution miracle pour l'animation)
                    this.model.head.copyFrom(castedOriginal.head);
                    this.model.body.copyFrom(castedOriginal.body);
                    this.model.rightArm.copyFrom(castedOriginal.rightArm);
                    this.model.leftArm.copyFrom(castedOriginal.leftArm);
                    this.model.rightLeg.copyFrom(castedOriginal.rightLeg);
                    this.model.leftLeg.copyFrom(castedOriginal.leftLeg);
                }

                // 3. On synchronise nos parties CUSTOM (head2, body2...) avec les parties standards
                this.model.syncParts();

                return this.model;
            }
        });
    }

    private static String getSingleLayerTexture(DyeColor color) {
        return CreateNuclear.asResource("textures/models/armor/" + color.getName() + "_anti_radiation_suit.png").toString();
    }
  
    public static class Helmet extends ArmorItem {
        protected final DyeColor color;

        private final Multimap<Attribute, AttributeModifier> attributeModifiers;

        public Helmet(Properties properties, DyeColor color) {
            super(ARMOR_MATERIAL, HELMET, properties);
            this.color = color;
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(HELMET.getSlot()));
            irradiatedArmorAttribute(builder, HELMET);
            UUID uuid = ARMOR_MODIFIER_UUID_PER_TYPE.get(HELMET);
            builder.put(CNAttributes.IRRADIATED_RESISTANCE.get(), new AttributeModifier(uuid, "Armor Resistance Irradiation", 42, AttributeModifier.Operation.ADDITION));
            this.attributeModifiers = builder.build();
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return getSingleLayerTexture(this.color);
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            AntiRadiationArmorItem.attachCustomModel(consumer);
        }

        public static class DyeItemHelmetList<T extends Helmet> implements Iterable<ItemEntry<T>> {
            private static final int COLOR_AMOUNT = DyeColor.values().length;
            private final ItemEntry<?>[] entry = new ItemEntry<?>[COLOR_AMOUNT];

            public DyeItemHelmetList(Function<DyeColor, ItemEntry<? extends T>> filler) {
                for (DyeColor color : DyeColor.values()) {
                    entry[color.ordinal()] = filler.apply(color);
                }
            }
            @SuppressWarnings("unchecked")
            public ItemEntry<T> get(DyeColor color) {
                return (ItemEntry<T>) entry[color.ordinal()];
            }
            public boolean contains(Item block) {
                for (ItemEntry<?> entry : entry) {
                    if (entry.is(block)) return true;
                }
                return false;
            }
            @SuppressWarnings("unchecked")
            public ItemEntry<T>[] toArray() {
                return (ItemEntry<T>[]) Arrays.copyOf(entry, entry.length);
            }
            @Override
            public Iterator<ItemEntry<T>> iterator() {
                return new Iterator<>() {
                    private int index = 0;
                    @Override
                    public boolean hasNext() {
                        return index < entry.length;
                    }
                    @SuppressWarnings("unchecked")
                    @Override
                    public ItemEntry<T> next() {
                        if (!hasNext()) throw new NoSuchElementException();
                        return (ItemEntry<T>) entry[index++];
                    }
                };
            }
        }

        public static TagKey<Item> getHelmetTag(String key) {
            return key.equals("white")
                    ? CNItemTags.ANTI_RADIATION_ARMOR.tag
                    : CNItemTags.ANTI_RADIATION_HELMET_DYE.tag;
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
            return pEquipmentSlot == this.type.getSlot() ? this.attributeModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
        }
    }

    public static class Chestplate extends ArmorItem {
        protected final DyeColor color;

        private final Multimap<Attribute, AttributeModifier> attributeModifiers;


        public Chestplate(Properties properties, DyeColor color) {
            super(ARMOR_MATERIAL, CHESTPLATE, properties);
            this.color = color;

            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(CHESTPLATE.getSlot()));
            irradiatedArmorAttribute(builder, CHESTPLATE);
            this.attributeModifiers = builder.build();
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return getSingleLayerTexture(this.color);
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            AntiRadiationArmorItem.attachCustomModel(consumer);
        }

        public static class DyeItemChestplateList<T extends Chestplate> implements Iterable<ItemEntry<T>> {
            private static final int COLOR_AMOUNT = DyeColor.values().length;
            private final ItemEntry<?>[] entry = new ItemEntry<?>[COLOR_AMOUNT];

            public DyeItemChestplateList(Function<DyeColor, ItemEntry<? extends T>> filler) {
                for (DyeColor color : DyeColor.values()) {
                    entry[color.ordinal()] = filler.apply(color);
                }
            }
            @SuppressWarnings("unchecked")
            public ItemEntry<T> get(DyeColor color) {
                return (ItemEntry<T>) entry[color.ordinal()];
            }
            public boolean contains(Item block) {
                for (ItemEntry<?> entry : entry) {
                    if (entry.is(block)) return true;
                }
                return false;
            }
            @SuppressWarnings("unchecked")
            public ItemEntry<T>[] toArray() {
                return (ItemEntry<T>[]) Arrays.copyOf(entry, entry.length);
            }
            @Override
            public Iterator<ItemEntry<T>> iterator() {
                return new Iterator<>() {
                    private int index = 0;
                    @Override
                    public boolean hasNext() {
                        return index < entry.length;
                    }
                    @SuppressWarnings("unchecked")
                    @Override
                    public ItemEntry<T> next() {
                        if (!hasNext()) throw new NoSuchElementException();
                        return (ItemEntry<T>) entry[index++];
                    }
                };
            }
        }

        public static TagKey<Item> getChestplateTag(String key) {
            return key.equals("white")
                    ? CNItemTags.ANTI_RADIATION_ARMOR.tag
                    : CNItemTags.ANTI_RADIATION_CHESTPLATE_DYE.tag;
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
            return pEquipmentSlot == this.type.getSlot() ? this.attributeModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
        }
    }

    public static class Leggings extends ArmorItem {
        protected final DyeColor color;

        private final Multimap<Attribute, AttributeModifier> attributeModifiers;


        public Leggings(Properties properties, DyeColor color) {
            super(ARMOR_MATERIAL, LEGGINGS, properties);
            this.color = color;
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(LEGGINGS.getSlot()));
            irradiatedArmorAttribute(builder, LEGGINGS);
            this.attributeModifiers = builder.build();
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return getSingleLayerTexture(this.color);
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            AntiRadiationArmorItem.attachCustomModel(consumer);
        }

        public static class DyeItemLeggingsList<T extends Leggings> implements Iterable<ItemEntry<T>> {
            private static final int COLOR_AMOUNT = DyeColor.values().length;
            private final ItemEntry<?>[] entry = new ItemEntry<?>[COLOR_AMOUNT];

            public DyeItemLeggingsList(Function<DyeColor, ItemEntry<? extends T>> filler) {
                for (DyeColor color : DyeColor.values()) {
                    entry[color.ordinal()] = filler.apply(color);
                }
            }
            @SuppressWarnings("unchecked")
            public ItemEntry<T> get(DyeColor color) {
                return (ItemEntry<T>) entry[color.ordinal()];
            }
            public boolean contains(Item block) {
                for (ItemEntry<?> entry : entry) {
                    if (entry.is(block)) return true;
                }
                return false;
            }
            @SuppressWarnings("unchecked")
            public ItemEntry<T>[] toArray() {
                return (ItemEntry<T>[]) Arrays.copyOf(entry, entry.length);
            }
            @Override
            public Iterator<ItemEntry<T>> iterator() {
                return new Iterator<>() {
                    private int index = 0;
                    @Override
                    public boolean hasNext() {
                        return index < entry.length;
                    }
                    @SuppressWarnings("unchecked")
                    @Override
                    public ItemEntry<T> next() {
                        if (!hasNext()) throw new NoSuchElementException();
                        return (ItemEntry<T>) entry[index++];
                    }
                };
            }
        }

        public static TagKey<Item> getLeggingsTag(String key) {
            return key.equals("white")
                    ? CNItemTags.ANTI_RADIATION_ARMOR.tag
                    : CNItemTags.ANTI_RADIATION_LEGGINGS_DYE.tag;
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
            return pEquipmentSlot == this.type.getSlot() ? this.attributeModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
        }
    }

    public static class Boot extends ArmorItem {
        private final Multimap<Attribute, AttributeModifier> attributeModifiers;
        protected final DyeColor color;
        public Boot(Properties properties, DyeColor color) {
            super(ARMOR_MATERIAL, BOOTS, properties);

            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(BOOTS.getSlot()));
            irradiatedArmorAttribute(builder, BOOTS);
            this.attributeModifiers = builder.build();
            this.color = color;
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return String.valueOf(CreateNuclear.asResource("textures/models/armor/white_anti_radiation_suit_layer_1.png"));
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
            return pEquipmentSlot == this.type.getSlot() ? this.attributeModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
            //return getSingleLayerTexture(this.color);
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            AntiRadiationArmorItem.attachCustomModel(consumer);
        }

        public static class DyeItemBootList<T extends Boot> implements Iterable<ItemEntry<T>> {
            private static final int COLOR_AMOUNT = DyeColor.values().length;
            private final ItemEntry<?>[] entry = new ItemEntry<?>[COLOR_AMOUNT];

            public DyeItemBootList(Function<DyeColor, ItemEntry<? extends T>> filler) {
                for (DyeColor color : DyeColor.values()) {
                    entry[color.ordinal()] = filler.apply(color);
                }
            }
            @SuppressWarnings("unchecked")
            public ItemEntry<T> get(DyeColor color) {
                return (ItemEntry<T>) entry[color.ordinal()];
            }
            public boolean contains(Item block) {
                for (ItemEntry<?> entry : entry) {
                    if (entry.is(block)) return true;
                }
                return false;
            }
            @SuppressWarnings("unchecked")
            public ItemEntry<T>[] toArray() {
                return (ItemEntry<T>[]) Arrays.copyOf(entry, entry.length);
            }
            @Override
            public Iterator<ItemEntry<T>> iterator() {
                return new Iterator<>() {
                    private int index = 0;
                    @Override
                    public boolean hasNext() {
                        return index < entry.length;
                    }
                    @SuppressWarnings("unchecked")
                    @Override
                    public ItemEntry<T> next() {
                        if (!hasNext()) throw new NoSuchElementException();
                        return (ItemEntry<T>) entry[index++];
                    }
                };
            }
        }

        public static TagKey<Item> getBootTag(String key) {
            return key.equals("white")
                    ? CNItemTags.ANTI_RADIATION_ARMOR.tag
                    : CNItemTags.ANTI_RADIATION_BOOTS_DYE.tag;
        }
    }

    public enum Armor {
        WHITE_ARMOR(DyeColor.WHITE),
        YELLOW_ARMOR(DyeColor.YELLOW),
        RED_ARMOR(DyeColor.RED),
        BLUE_ARMOR(DyeColor.BLUE),
        GREEN_ARMOR(DyeColor.GREEN),
        BLACK_ARMOR(DyeColor.BLACK),
        ORANGE_ARMOR(DyeColor.ORANGE),
        PURPLE_ARMOR(DyeColor.PURPLE),
        BROWN_ARMOR(DyeColor.BROWN),
        PINK_ARMOR(DyeColor.PINK),
        CYAN_ARMOR(DyeColor.CYAN),
        LIGHT_GRAY_ARMOR(DyeColor.LIGHT_GRAY),
        GRAY_ARMOR(DyeColor.GRAY),
        LIGHT_BLUE_ARMOR(DyeColor.LIGHT_BLUE),
        LIME_ARMOR(DyeColor.LIME),
        MAGENTA_ARMOR(DyeColor.MAGENTA);

        private static final Map<DyeColor, ItemEntry<Helmet>> helmetMap = new EnumMap<>(DyeColor.class);
        private static final Map<DyeColor, ItemEntry<Chestplate>> chestplateMap = new EnumMap<>(DyeColor.class);
        private static final Map<DyeColor, ItemEntry<Leggings>> leggingsMap = new EnumMap<>(DyeColor.class);
        private static final Map<DyeColor, ItemEntry<Boot>> bootsMap = new EnumMap<>(DyeColor.class);

        static {
            for (DyeColor color : DyeColor.values()) {
                helmetMap.put(color, CNItems.ANTI_RADIATION_HELMETS.get(color));
                chestplateMap.put(color, CNItems.ANTI_RADIATION_CHESTPLATES.get(color));
                leggingsMap.put(color, CNItems.ANTI_RADIATION_LEGGINGS.get(color));
                bootsMap.put(color, CNItems.ANTI_RADIATION_BOOTS.get(color));
            }
        }

        private final DyeColor color;

        Armor(DyeColor dyeColor) {
            this.color = dyeColor;
        }

        public ItemEntry<Helmet> getHelmetItem() {
            return helmetMap.get(this.color);
        }
        public static ItemEntry<Helmet> getHelmetByColor(DyeColor color) {
            return helmetMap.get(color);
        }

        public ItemEntry<Chestplate> getChestplateItem() {
            return chestplateMap.get(this.color);
        }
        public static ItemEntry<Chestplate> getChestplateByColor(DyeColor color) {
            return chestplateMap.get(color);
        }

        public ItemEntry<Leggings> getLeggingsItem() {
            return leggingsMap.get(this.color);
        }
        public static ItemEntry<Leggings> getLeggingsByColor(DyeColor color) {
            return leggingsMap.get(color);
        }

        public ItemEntry<Boot> getBootItem() {
            return bootsMap.get(this.color);
        }
        public static ItemEntry<Boot> getBootByColor(DyeColor color) {
            return bootsMap.get(color);
        }

        public static boolean isArmored(ItemStack item) {
            return helmetMap.values().stream().anyMatch(entry -> entry.is(item.getItem())) ||
                    chestplateMap.values().stream().anyMatch(entry -> entry.is(item.getItem())) ||
                    leggingsMap.values().stream().anyMatch(entry -> entry.is(item.getItem())) ||
                    bootsMap.values().stream().anyMatch(entry -> entry.is(item.getItem()));
        }

        public static boolean isArmored2(ItemStack item) {
            return CNItems.ANTI_RADIATION_HELMETS.contains(item.getItem())
                    || CNItems.ANTI_RADIATION_CHESTPLATES.contains(item.getItem())
                    || CNItems.ANTI_RADIATION_LEGGINGS.contains(item.getItem())
                    || CNItems.ANTI_RADIATION_BOOTS.contains(item.getItem());
        }
    }
}