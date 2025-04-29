package net.nuclearteam.createnuclear.foundation.events.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.foundation.events.EasingHudOverlay;
import net.nuclearteam.createnuclear.foundation.events.HudOverlayRegistry;
import net.nuclearteam.createnuclear.foundation.utility.RenderHelper;

/**
 * HUD overlay for radiation effect when the player is irradiated.
 */
public class RadiationOverlay extends EasingHudOverlay {
    static {
        // Auto-register this overlay when the class is loaded
        HudOverlayRegistry.register(new RadiationOverlay());
    }

    private static final ResourceLocation RADIATION_TEXTURE = CreateNuclear.asResource("textures/misc/irradiated_vision.png");
    private static float coverage = 1f;

    /**
     * Updates the coverage scale for the radiation effect.
     * @param newCoverage scale factor (1.0 = normal size)
     */
    public static void setCoverage(float newCoverage) {
        coverage = newCoverage;
    }

    @Override
    public ResourceLocation getAfterOverlay() {
        return VanillaGuiOverlay.HELMET.id();
    }

    @Override
    public boolean isActive() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.getEffect(CNEffects.RADIATION.get()) != null;
    }

    @Override
    protected void renderWithAlpha(GuiGraphics graphics, float partialTicks, float alpha) {
        // Render radiation overlay with dynamic coverage and alpha
        RenderHelper.renderTextureOverlay(graphics, RADIATION_TEXTURE, alpha, coverage);
    }

    @Override
    public int getPriority() {
        return 100; // Fixed background priority for radiation effect
    }
}
