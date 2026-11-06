package net.nuclearteam.createnuclear.content.equipment.armor;


import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class AntiRadiationArmorModel extends HumanoidModel<LivingEntity> {
    public AntiRadiationArmorModel(ModelPart root, EquipmentSlot slot) {
        super(root);
        this.head.getChild("helmet").visible = slot == EquipmentSlot.HEAD;
        this.body.getChild("chestplate").visible = slot == EquipmentSlot.CHEST;
        this.rightArm.getChild("sleeve").visible = slot == EquipmentSlot.CHEST;
        this.leftArm.getChild("sleeve").visible = slot == EquipmentSlot.CHEST;
        this.rightLeg.getChild("leggings").visible = slot == EquipmentSlot.LEGS;
        this.leftLeg.getChild("leggings").visible = slot == EquipmentSlot.LEGS;
        this.rightLeg.getChild("boot").visible = slot == EquipmentSlot.FEET;
        this.leftLeg.getChild("boot").visible = slot == EquipmentSlot.FEET;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // HumanoidModel's constructor requires a "hat" child; keep it empty.
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(30, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.2F))
                .texOffs(30, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.ZERO);

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(0, 19).addBox(-3.7F, 0.0F, -2.8F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.4F, -1.0F, -3.1F, 9.0F, 13.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-4.0F, 0.0F, -2.7F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(64, 0).addBox(-4.5F, -1.0F, -3.0F, 9.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        right_arm.addOrReplaceChild("sleeve", CubeListBuilder.create().texOffs(45, 32).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)).mirror(false)
                .texOffs(29, 32).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.55F)).mirror(false), PartPose.ZERO);

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        left_arm.addOrReplaceChild("sleeve", CubeListBuilder.create().texOffs(45, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
                .texOffs(29, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.ZERO);

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        right_leg.addOrReplaceChild("leggings", CubeListBuilder.create().texOffs(29, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(45, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.ZERO);
        right_leg.addOrReplaceChild("boot", CubeListBuilder.create().texOffs(61, 39).mirror().addBox(-2.0F, 8.3F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.ZERO);

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        left_leg.addOrReplaceChild("leggings", CubeListBuilder.create().texOffs(29, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(45, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.ZERO);
        left_leg.addOrReplaceChild("boot", CubeListBuilder.create().texOffs(61, 39).addBox(-2.0F, 8.3F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 96, 96);
    }
}
