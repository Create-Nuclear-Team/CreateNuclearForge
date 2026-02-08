package net.nuclearteam.createnuclear.content.equipment.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class AntiRadiationArmorModeldfggh extends HumanoidModel<LivingEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public final ModelPart head;
	public final ModelPart body;
	public final ModelPart right_arm;
	public final ModelPart left_arm;
	public final ModelPart right_leg;
	public final ModelPart left_leg;

	public AntiRadiationArmorModeldfggh(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.right_arm = root.getChild("right_arm");
		this.left_arm = root.getChild("left_arm");
		this.right_leg = root.getChild("right_leg");
		this.left_leg = root.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
		.texOffs(30, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.2F))
		.texOffs(30, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -8.0F, 1.0F));

		// PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
		// .texOffs(0, 19).addBox(-3.875F, -6.175F, -2.65F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
		// .texOffs(0, 0).addBox(-4.575F, -7.175F, -2.95F, 9.0F, 13.0F, 6.0F, new CubeDeformation(0.0F))
		// .texOffs(0, 36).addBox(-3.875F, -6.175F, -2.55F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
		// .texOffs(64, 0).addBox(-4.675F, -7.175F, -2.85F, 9.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0F, 0F, 0F));

		// PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
		// .texOffs(45, 32).mirror().addBox(-2.1F, -7.5F, -1.9F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		// .texOffs(29, 32).mirror().addBox(-1.9F, -7.5F, -2.1F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(-5.0F, 2.0F, 0.0F));

		// PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
		// .texOffs(45, 32).addBox(-2.1F, -6.55F, -1.9F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		// .texOffs(29, 32).addBox(-1.9F, -6.55F, -2.1F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

		// PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
		// .texOffs(29, 48).mirror().addBox(-2.0733F, -7.8167F, -1.8667F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		// .texOffs(45, 48).mirror().addBox(-1.9633F, -7.8167F, -2.0667F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false)
		// .texOffs(61, 39).mirror().addBox(-1.9633F, 0.1833F, -2.0667F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(-1.6367F, 19.5667F, -0.2333F));

		// PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
		// .texOffs(29, 48).addBox(-1.9667F, -7.8167F, -1.8667F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		// .texOffs(45, 48).addBox(-2.0167F, -7.8167F, -2.0667F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
		// .texOffs(61, 39).addBox(-2.0167F, 0.1833F, -2.0667F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(2.2167F, 19.5667F, -0.2333F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
//			.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
			.texOffs(16, 35).addBox(-3.875F, -6.175F, -2.65F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)),
//			.texOffs(0, 0).addBox(-4.575F, -7.175F, -2.95F, 9.0F, 13.0F, 6.0F, new CubeDeformation(0.0F))
//			.texOffs(0, 36).addBox(-3.875F, -6.175F, -2.55F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
//			.texOffs(64, 0).addBox(-4.675F, -7.175F, -2.85F, 9.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, 0.0F, 0.0F));

PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
        .texOffs(40, 16).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false),
        PartPose.offset(-5.0F, 2.0F, 0.0F));

PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
        .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
        PartPose.offset(5.0F, 2.0F, 0.0F));

PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
        .texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false),
        PartPose.offset(-1.9F, 12.0F, 0.0F));

PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
        PartPose.offset(1.9F, 12.0F, 0.0F));


		return LayerDefinition.create(meshdefinition, 96, 96);
	}


	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}