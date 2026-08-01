package net.nuclearteam.createnuclear.content.equipment.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.CNModelLayers;
import org.jetbrains.annotations.NotNull;

public final class AntiRadiationArmorClientExtensions implements IClientItemExtensions {
    private AntiRadiationArmorModel model;

    @Override
    public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        if (this.model == null) {
            EntityModelSet entityModelSet = Minecraft.getInstance().getEntityModels();
            ModelPart root = entityModelSet.bakeLayer(CNModelLayers.ANTI_IRRADIATION_ARMOR);
            this.model = new AntiRadiationArmorModel(root);
        }

        // Forge copies the body pose + standard part visibility from `original` onto the
        // model after this returns, but it can't tell LEGS from FEET (both make the vanilla
        // legs visible) and knows nothing about our custom boot parts. We pass the slot so
        // renderToBuffer can show legs for leggings vs. only boots for the FEET slot.
        this.model.currentSlot = equipmentSlot;
        return this.model;
    }
}
