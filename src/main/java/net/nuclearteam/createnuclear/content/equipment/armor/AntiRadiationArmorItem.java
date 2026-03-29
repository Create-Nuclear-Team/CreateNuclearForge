package net.nuclearteam.createnuclear.content.equipment.armor;

import com.simibubi.create.AllKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.equipment.cloth.ClothItem;
import net.nuclearteam.createnuclear.foundation.util.ClothTagHelper;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class AntiRadiationArmorItem extends ArmorItem {
    protected final DyeColor color;

    // Constructeur adapté à ce que tu utilises dans ton Registrate
    public AntiRadiationArmorItem(ArmorMaterial material, Type type, Properties properties, DyeColor color) {
        super(material, type, properties);
        this.color = color;
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

    public static class Helmet extends AntiRadiationArmorItem {
        public Helmet(Properties p, DyeColor color) {
            super(ArmorMaterials.ANTI_RADIATION_SUIT, Type.HELMET, p, color);
        }
    }

    public static class Chestplate extends AntiRadiationArmorItem {
        public Chestplate(Properties p, DyeColor color) {
            super(ArmorMaterials.ANTI_RADIATION_SUIT, Type.CHESTPLATE, p, color);
        }
    }

    public static class Leggings extends AntiRadiationArmorItem {
        public Leggings(Properties p, DyeColor color) {
            super(ArmorMaterials.ANTI_RADIATION_SUIT, Type.LEGGINGS, p, color);
        }
    }

    public static class Boot extends AntiRadiationArmorItem {
        public Boot(Properties p, DyeColor color) {
            super(ArmorMaterials.ANTI_RADIATION_SUIT, Type.BOOTS, p, color);
        }
    }
}