package net.nuclearteam.createnuclear.foundation.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.foundation.events.overlay.IrradiatedOverlayRendererVision;

@EventBusSubscriber(modid = CreateNuclear.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CNClientEvent {
    private static final HudRenderer HUD_RENDERER = new HudRenderer();

    @SubscribeEvent
    public static void onRegisterGui(RegisterGuiOverlaysEvent event) {
        HUD_RENDERER.onHudRender(event);
        event.registerAbove(VanillaGuiOverlay.HELMET.id(), "irradiated_vision", IrradiatedOverlayRendererVision.OVERLAY);
    }


}
