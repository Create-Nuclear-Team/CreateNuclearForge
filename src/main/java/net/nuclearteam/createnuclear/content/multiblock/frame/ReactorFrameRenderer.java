package net.nuclearteam.createnuclear.content.multiblock.frame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.platform.ForgeCatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.fluids.FluidStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;

/**
 * Renders the reactor fluid dynamically inside the {@link ReactorFrame} window.
 * The fluid (texture + tint) is resolved from the owning reactor controller, so
 * the visible liquid reflects whichever fluid the reactor actually uses
 * (water, liquid nitrogen, ...) instead of a texture baked into the model.
 */
public class ReactorFrameRenderer extends SafeBlockEntityRenderer<ReactorFrameEntity> {

    // Horizontal interior of the window (1..15 px on a 16 px block).
    private static final float X_MIN = 1f / 16f;
    private static final float X_MAX = 15f / 16f;
    private static final float Z_MIN = 1f / 16f;
    private static final float Z_MAX = 15f / 16f;

    public ReactorFrameRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    protected void renderSafe(ReactorFrameEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        ReactorControllerBlockEntity controller = be.getControllerEntity();
        if (controller == null) return;

        FluidStack fluid = controller.getDisplayedFluid();
        if (fluid == null || fluid.isEmpty()) return;

        // Vertical bounds match the fluid volume that used to be baked into each
        // frame part model (frame_top / frame_middle / frame_bottom / frame_none).
        float yMin;
        float yMax;
        switch (be.getBlockState().getValue(ReactorFrame.PART)) {
            case START -> { yMin = 0f;          yMax = 9f / 16f; }
            case MIDDLE -> { yMin = 0f;         yMax = 1f; }
            case END -> { yMin = 4f / 16f;      yMax = 1f; }
            default -> { yMin = 2.9f / 16f;     yMax = 9.9f / 16f; }
        }

        ForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid,
                X_MIN, yMin, Z_MIN, X_MAX, yMax, Z_MAX,
                buffer, ms, light, false, true);
    }
}
