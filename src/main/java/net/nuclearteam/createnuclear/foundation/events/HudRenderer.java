package net.nuclearteam.createnuclear.foundation.events;

import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.nuclearteam.createnuclear.foundation.events.overlay.EventTextOverlay;
import net.nuclearteam.createnuclear.foundation.events.overlay.HelmetOverlay;
import net.nuclearteam.createnuclear.foundation.events.overlay.HudOverlay;
import net.nuclearteam.createnuclear.foundation.events.overlay.RadiationOverlay;

import java.util.Comparator;
import java.util.List;

public class HudRenderer {
    private static final List<HudOverlay> overlays = List.of(
            new HelmetOverlay(),
            new RadiationOverlay(),
            new EventTextOverlay()
    );

    public void onHudRender(RegisterGuiOverlaysEvent event) {
        overlays.stream()
                .sorted(Comparator.comparingInt(HudOverlay::getPriority))
                .forEach(overlay -> overlay.register(event));
    }
}
