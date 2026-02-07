package net.nuclearteam.createnuclear.content.equipment.armor; // Mets ton package

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.CNModelLayers;
import org.jetbrains.annotations.NotNull;

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
        consumer.accept(new IClientItemExtensions() {
            private AntiRadiationArmorModel model;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.model == null) {
                    var entityModelSet = Minecraft.getInstance().getEntityModels();
                    var root = entityModelSet.bakeLayer(CNModelLayers.ANTI_IRRADIATION_ARMOR);
                    this.model = new AntiRadiationArmorModel(root);
                }

                // 1. On donne l'info du slot au modèle (pour le fix du renderToBuffer)
                this.model.currentSlot = equipmentSlot;

                // 2. On reset tout
                this.model.setAllVisible(false);
//                this.model.rightLegArmor.visible = false;
//                this.model.rightBootArmor.visible = false;
//                this.model.leftLegArmor.visible = false;
//                this.model.leftBootArmor.visible = false;

                // 3. Logique d'activation
                switch (equipmentSlot) {
                    case HEAD -> {
                        this.model.getHead().visible = true;
                        this.model.hat.visible = true;
                    }
                    case CHEST -> {
                        this.model.body.visible = true;
                        this.model.rightArm.visible = true;
                        this.model.leftArm.visible = true;
                    }
                    case LEGS -> {
                        this.model.rightLeg.visible = true;
                        this.model.leftLeg.visible = true;

                        // On active juste la partie "Cuisse"
//                        this.model.rightLegArmor.visible = true;
//                        this.model.leftLegArmor.visible = true;
                    }
                    case FEET -> {
                        this.model.rightLeg.visible = true;
                        this.model.leftLeg.visible = true;

                        // On active juste la partie "Botte"
//                        this.model.rightBootArmor.visible = true;
//                        this.model.leftBootArmor.visible = true;
                    }
                }

                return this.model;
            }
        });
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