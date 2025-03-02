package net.nuclearteam.createnuclear.foundation.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.nuclearteam.createnuclear.CNEntityType;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class CommentEventClients {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        CNEntityType.registerModelLayer(event);
    }
}
