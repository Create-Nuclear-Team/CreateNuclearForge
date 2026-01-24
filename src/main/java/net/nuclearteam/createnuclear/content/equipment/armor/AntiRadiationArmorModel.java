package net.nuclearteam.createnuclear.content.equipment.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.nuclearteam.createnuclear.CreateNuclear;

public class AntiRadiationArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(CreateNuclear.MOD_ID, "anti_radiation_armor"), "main");

    private final ModelPart armorRoot;
    private final ModelPart head2;
    private final ModelPart body2;
    private final ModelPart rightArm2;
    private final ModelPart leftArm2;
    private final ModelPart rightLeg2;
    private final ModelPart leftLeg2;

    public AntiRadiationArmorModel(ModelPart root) {
        super(root);
        this.armorRoot = root.getChild("Armor_2");
        this.head2 = this.armorRoot.getChild("Head2");
        this.body2 = this.armorRoot.getChild("Body2");
        this.rightArm2 = this.armorRoot.getChild("Right_Arm2");
        this.leftArm2 = this.armorRoot.getChild("Left_Arm2");
        this.rightLeg2 = this.armorRoot.getChild("Right_Leg2");
        this.leftLeg2 = this.armorRoot.getChild("Left_Leg2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Parties standards (vides mais requises)
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

        // Votre structure Custom
        PartDefinition Armor_2 = partdefinition.addOrReplaceChild("Armor_2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        Armor_2.addOrReplaceChild("Head2", CubeListBuilder.create().texOffs(30, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
                .texOffs(30, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.2F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        Armor_2.addOrReplaceChild("Body2", CubeListBuilder.create().texOffs(0, 19).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(1.0F))
                .texOffs(0, 0).addBox(-4.5F, -0.5F, -2.5F, 9.0F, 13.0F, 5.0F, new CubeDeformation(1.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        Armor_2.addOrReplaceChild("Right_Arm2", CubeListBuilder.create().texOffs(45, 32).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.0F)).mirror(false), PartPose.offset(-5.0F, 2.0F, 0.0F));
        Armor_2.addOrReplaceChild("Left_Arm2", CubeListBuilder.create().texOffs(45, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));
        Armor_2.addOrReplaceChild("Right_Leg2", CubeListBuilder.create().texOffs(29, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.0F)).mirror(false), PartPose.offset(-1.9F, 12.0F, 0.0F));
        Armor_2.addOrReplaceChild("Left_Leg2", CubeListBuilder.create().texOffs(29, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 96, 96);
    }

    // NOUVELLE MÉTHODE : On force la copie des positions/visibilité
    public void syncParts() {
        this.head2.copyFrom(this.head);
        this.body2.copyFrom(this.body);
        this.rightArm2.copyFrom(this.rightArm);
        this.leftArm2.copyFrom(this.leftArm);
        this.rightLeg2.copyFrom(this.rightLeg);
        this.leftLeg2.copyFrom(this.leftLeg);

        this.head2.visible = this.head.visible;
        this.body2.visible = this.body.visible;
        this.rightArm2.visible = this.rightArm.visible;
        this.leftArm2.visible = this.leftArm.visible;
        this.rightLeg2.visible = this.rightLeg.visible;
        this.leftLeg2.visible = this.leftLeg.visible;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        syncParts(); // On appelle aussi ici au cas où le jeu l'utilise normalement
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.armorRoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}