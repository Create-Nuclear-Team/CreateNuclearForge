package net.nuclearteam.createnuclear.content.equipment.armor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.CreateNuclear;

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
        CompoundTag tag = stack.getTag();
        String cloth = (tag != null && tag.contains("Cloth")) ? tag.getString("Cloth") : "default";
        String textureFile = cloth + "_anti_radiation_suit.png";
        return CreateNuclear.asResource("textures/models/armor/" + textureFile).toString();
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