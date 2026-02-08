package net.nuclearteam.createnuclear.content.equipment.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;


public class AntiRadiationArmorModeltest extends HumanoidModel<LivingEntity> {
	private final ModelPart head;
	public final ModelPart body;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;

	public final ModelPart right_leg_armor;
	public final ModelPart right_boot;
	public final ModelPart left_leg_armor;
	public final ModelPart left_boot;

	public EquipmentSlot currentSlot = EquipmentSlot.HEAD;

	public AntiRadiationArmorModeltest(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.hat.copyFrom(this.head);
		this.body = root.getChild("body");
		this.right_arm = root.getChild("right_arm");
		this.left_arm = root.getChild("left_arm");
		this.right_leg = root.getChild("right_leg");
		this.left_leg = root.getChild("left_leg");
		this.right_leg_armor = this.right_leg.getChild("power_leg_outer");
		this.right_boot = this.right_leg.getChild("power_boot");
		this.left_leg_armor = this.left_leg.getChild("power_leg_outer");
		this.left_boot = this.left_leg.getChild("power_boot");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(30, 0).addBox(-4.0F, -4.075F, -3.875F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(30, 16).addBox(-4.0F, -3.925F, -4.125F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.05F, -3.775F, -0.175F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 19).addBox(-3.875F, -5.875F, -2.65F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-4.575F, -6.575F, -2.95F, 9.0F, 13.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 36).addBox(-3.875F, -5.875F, -2.55F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(64, 0).addBox(-4.675F, -6.675F, -2.85F, 9.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.175F, 6.175F, -0.15F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(45, 32).mirror().addBox(-2.1F, -6.0F, -1.9F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(29, 32).mirror().addBox(-1.9F, -6.0F, -2.1F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(-5.8F, 6.0F, -0.2F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(45, 32).addBox(-2.1F, -5.95F, -1.9F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(29, 32).addBox(-1.9F, -6.05F, -2.1F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(6.2F, 6.05F, -0.2F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(29, 48).mirror().addBox(-2.0733F, -7.4667F, -1.8667F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(45, 48).mirror().addBox(-1.9633F, -7.2667F, -2.0667F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false)
				.texOffs(61, 39).mirror().addBox(-1.9633F, 0.7333F, -2.0667F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(-1.6367F, 19.5667F, -0.2333F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(29, 48).addBox(-1.9667F, -7.4667F, -1.8667F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(45, 48).addBox(-2.0167F, -7.2667F, -2.0667F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
				.texOffs(61, 39).addBox(-2.0167F, 0.7333F, -2.0667F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(2.2167F, 19.5667F, -0.2333F));

		right_leg.addOrReplaceChild("power_boot", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 32).addBox(-2.0F, 8.25F, -2.0F, 4.0F, 3.75F, 4.0F, new CubeDeformation(0.25F)), PartPose.ZERO);

		left_leg.addOrReplaceChild("power_boot", CubeListBuilder.create()
				.texOffs(16, 48).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 48).addBox(-2.0F, 8.25F, -2.0F, 4.0F, 3.75F, 4.0F, new CubeDeformation(0.25F)), PartPose.ZERO);

		return LayerDefinition.create(meshdefinition, 96, 96);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		if (this.currentSlot == EquipmentSlot.LEGS) {
			this.body.visible = false;
		}

		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		this.right_leg_armor.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		this.right_boot.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		this.left_leg_armor.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		this.left_boot.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);

	}
}