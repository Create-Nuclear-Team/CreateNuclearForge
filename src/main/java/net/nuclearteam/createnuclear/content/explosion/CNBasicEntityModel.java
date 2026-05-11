package net.nuclearteam.createnuclear.content.explosion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public abstract class CNBasicEntityModel<T extends Entity> extends EntityModel<T> {
    public int textureWidth;
    public int textureHeight;

    protected CNBasicEntityModel() {
        this(RenderType::entityCutoutNoCull);
    }

    protected CNBasicEntityModel(Function<ResourceLocation, RenderType> p_102613_) {
        super(p_102613_);
        this.textureWidth = 64;
        this.textureHeight = 32;
    }

    public void renderToBuffer(PoseStack p_103013_, VertexConsumer p_103014_, int p_103015_, int p_103016_, float p_103017_, float p_103018_, float p_103019_, float p_103020_) {
        this.parts().forEach((p_103030_) -> p_103030_.render(p_103013_, p_103014_, p_103015_, p_103016_, p_103017_, p_103018_, p_103019_, p_103020_));
    }

    public abstract Iterable<CNBasicModelPart> parts();

    public abstract void setupAnim(T var1, float var2, float var3, float var4, float var5, float var6);

    public void prepareMobModel(T p_102614_, float p_102615_, float p_102616_, float p_102617_) {
    }
}