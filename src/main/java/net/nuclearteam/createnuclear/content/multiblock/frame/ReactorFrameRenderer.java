package net.nuclearteam.createnuclear.content.multiblock.frame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ReactorFrameRenderer extends SafeBlockEntityRenderer<ReactorFrameEntity> {

    public ReactorFrameRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(ReactorFrameEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {

    }
}
