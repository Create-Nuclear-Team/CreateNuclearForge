package net.nuclearteam.createnuclear.content.equipment.armor; // Mets ton package

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.nuclearteam.createnuclear.CNTags;

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
        // Cela cherchera: assets/createnuclear/textures/models/armor/default_anti_radiation_suit.png (exemple)
//        return "createnuclear:textures/models/armor/" + color.getSerializedName() + "_anti_radiation_suit.png";
        return "createnuclear:textures/models/armor/default_anti_radiation_suit.png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new AntiRadiationArmorClientExtensions());
    }

    public static class Helmet extends AntiRadiationArmorItem {
        public Helmet(Properties p, DyeColor color) {
            // Remplace 'CNMaterials.ANTI_RADIATION' par la référence exacte vers ton ArmorMaterial
            super(ArmorMaterials.ANTI_RADIATION_SUIT, Type.HELMET, p, color);
        }

        public static TagKey<Item> getHelmetTag(String key) {
            return key.equals("white")
                    ? CNTags.CNItemTags.ANTI_RADIATION_ARMOR.tag
                    : CNTags.CNItemTags.ANTI_RADIATION_HELMET_DYE.tag;
        }
    }

    public static class Chestplate extends AntiRadiationArmorItem {
        public Chestplate(Properties p, DyeColor color) {
            super(ArmorMaterials.ANTI_RADIATION_SUIT, Type.CHESTPLATE, p, color);
        }

        public static TagKey<Item> getChestplateTag(String key) {
            return key.equals("white")
                    ? CNTags.CNItemTags.ANTI_RADIATION_ARMOR.tag
                    : CNTags.CNItemTags.ANTI_RADIATION_CHESTPLATE_DYE.tag;
        }
    }

    public static class Leggings extends AntiRadiationArmorItem {
        public Leggings(Properties p, DyeColor color) {
            super(ArmorMaterials.ANTI_RADIATION_SUIT, Type.LEGGINGS, p, color);
        }

        public static TagKey<Item> getLeggingsTag(String key) {
            return key.equals("white")
                    ? CNTags.CNItemTags.ANTI_RADIATION_ARMOR.tag
                    : CNTags.CNItemTags.ANTI_RADIATION_LEGGINGS_DYE.tag;
        }
    }

    public static class Boot extends AntiRadiationArmorItem {
        public Boot(Properties p, DyeColor color) {
            super(ArmorMaterials.ANTI_RADIATION_SUIT, Type.BOOTS, p, color);
        }

        public static TagKey<Item> getBootsTag(String key) {
            return key.equals("white")
                    ? CNTags.CNItemTags.ANTI_RADIATION_ARMOR.tag
                    : CNTags.CNItemTags.ANTI_RADIATION_BOOTS_DYE.tag;
        }
    }
}