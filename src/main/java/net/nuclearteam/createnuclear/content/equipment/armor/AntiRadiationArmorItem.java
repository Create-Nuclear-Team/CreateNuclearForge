package net.nuclearteam.createnuclear.content.equipment.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.AllKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancement;

import net.nuclearteam.createnuclear.CNAttributes;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.content.equipment.cloth.ClothItem;
import net.nuclearteam.createnuclear.foundation.utility.ClothTagHelper;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public abstract class AntiRadiationArmorItem extends ArmorItem {
    public static final double RADIATION_VALUE = 0.25;
    protected final DyeColor color;

    public AntiRadiationArmorItem(ArmorMaterials materials, Type type, Properties properties, DyeColor color) {
        super(materials, type, properties);
        this.color = color;
    }

    private static final EnumMap<ArmorItem.Type, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266744_) -> {
        p_266744_.put(ArmorItem.Type.BOOTS, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
        p_266744_.put(ArmorItem.Type.LEGGINGS, UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
        p_266744_.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
        p_266744_.put(ArmorItem.Type.HELMET, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
    });

    private static Multimap<Attribute, AttributeModifier> irradiatedArmorAttribute(Multimap<Attribute, AttributeModifier> map, ArmorItem.Type type) {
        UUID uuid = ARMOR_MODIFIER_UUID_PER_TYPE.get(type);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(map);
        // Use ADDITION so pieces add up to a resistance fraction (e.g. 0.25 per piece -> full set = 1.0)
        builder.put(CNAttributes.IRRADIATED_RESISTANCE.get(), new AttributeModifier(uuid, "Armor Resistance Irradiation", RADIATION_VALUE, AttributeModifier.Operation.ADDITION));

        return builder.build();
    }

    // Cette méthode indique au jeu où trouver la texture PNG de l'armure
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return ClothTagHelper.getArmorTexturePath(stack, "anti_radiation_suit.png");
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        if (AllKeys.shiftDown()) return;

        List<Component> makeSummary = makeSummary(pStack);
        if (makeSummary.isEmpty()) return;

        tooltip.add(CommonComponents.SPACE);
        tooltip.addAll(makeSummary);
    }


    public List<Component> makeSummary(ItemStack item) {
        if (!item.hasTag()) return Collections.emptyList();

        CompoundTag tag = item.getTag();
        ItemStack cloth = ItemStack.of(tag.getCompound(ClothTagHelper.ITEM));
        if (!(cloth.getItem() instanceof ClothItem clothItem)) return Collections.emptyList();

        List<Component> list = new ArrayList<>();
        CreateNuclearLang.translate("tooltip.cloth.color", clothItem.getColor().getSerializedName())
            .color(clothItem.getColor().getTextColor())
            .style(ChatFormatting.ITALIC)
            .addTo(list);

        return list;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new AntiRadiationArmorClientExtensions());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || !(entity instanceof Player player)) return;
        if (!stack.hasTag() || !stack.getTag().contains(ClothTagHelper.COLOR)) return;
        if (CNAdvancement.DYE_ANTI_RADIATION_ARMOR.isAlreadyAwardedTo(player)) return;
        CNAdvancement.DYE_ANTI_RADIATION_ARMOR.awardTo(player);
    }
  
    public static class Helmet extends AntiRadiationArmorItem implements IGoggleHelmet {
        public Helmet(Properties p, DyeColor color) {
            super(ArmorMaterials.ANTI_RADIATION_SUIT, Type.HELMET, p, color);
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
            if (slot != this.getType().getSlot()) return super.getDefaultAttributeModifiers(slot);
            Multimap<Attribute, AttributeModifier> map = super.getDefaultAttributeModifiers(slot);
            return AntiRadiationArmorItem.irradiatedArmorAttribute(map, Type.HELMET);
        }
    }

    public static class Chestplate extends AntiRadiationArmorItem {
        public Chestplate(Properties p, DyeColor color) {
            super(ArmorMaterials.ANTI_RADIATION_SUIT, Type.CHESTPLATE, p, color);
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
            if (slot != this.getType().getSlot()) return super.getDefaultAttributeModifiers(slot);
            Multimap<Attribute, AttributeModifier> map = super.getDefaultAttributeModifiers(slot);
            return AntiRadiationArmorItem.irradiatedArmorAttribute(map, Type.CHESTPLATE);
        }
    }

    public static class Leggings extends AntiRadiationArmorItem {
        public Leggings(Properties p, DyeColor color) {
            super(ArmorMaterials.ANTI_RADIATION_SUIT, Type.LEGGINGS, p, color);
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
            if (slot != this.getType().getSlot()) return super.getDefaultAttributeModifiers(slot);
            Multimap<Attribute, AttributeModifier> map = super.getDefaultAttributeModifiers(slot);
            return AntiRadiationArmorItem.irradiatedArmorAttribute(map, Type.LEGGINGS);
        }
    }

    public static class Boot extends AntiRadiationArmorItem {
        public Boot(Properties p, DyeColor color) {
            super(ArmorMaterials.ANTI_RADIATION_SUIT, Type.BOOTS, p, color);
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
            if (slot != this.getType().getSlot()) return super.getDefaultAttributeModifiers(slot);
            Multimap<Attribute, AttributeModifier> map = super.getDefaultAttributeModifiers(slot);
            return AntiRadiationArmorItem.irradiatedArmorAttribute(map, Type.BOOTS);
        }
    }

    public interface IGoggleHelmet {
        static boolean isGoggleHelmet(LivingEntity entity) {
            ItemStack headSlot = entity.getItemBySlot(EquipmentSlot.HEAD);
            return CNItems.ANTI_RADIATION_HELMETS.isIn(headSlot);
        }
    }
}