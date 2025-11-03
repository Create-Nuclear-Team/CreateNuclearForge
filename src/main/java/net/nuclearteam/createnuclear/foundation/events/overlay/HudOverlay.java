package net.nuclearteam.createnuclear.foundation.events.overlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.nuclearteam.createnuclear.CreateNuclear;

/**
 * Base interface for all HUD overlays.
 */
public interface HudOverlay {
    ResourceLocation getAfterOverlay();

    String getOverlayId();

    boolean isActive();

    int getPriority();

    IGuiOverlay getOverlay();

    void render(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height);

    default void register(RegisterGuiOverlaysEvent event) {
        event.registerAbove(
                getAfterOverlay(),
                getOverlayId(),
                getOverlay()
        );
    }
}
