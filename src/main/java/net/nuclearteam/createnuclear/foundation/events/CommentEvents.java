package net.nuclearteam.createnuclear.foundation.events;

import net.minecraft.world.entity.*;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nuclearteam.createnuclear.*;
import net.nuclearteam.createnuclear.content.effects.capability.RadiationCapability;

@Mod.EventBusSubscriber
public class CommentEvents {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase == Phase.START) return;
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        RadiationCapability.attach(event);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        RadiationCapability.onPlayerTick(event);
    }

    @Mod.EventBusSubscriber(modid = CreateNuclear.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RadiationEvent {
        @SubscribeEvent
        public static void onEntityAttribute(EntityAttributeModificationEvent event) {
            event.add(EntityType.PLAYER, CNAttributes.IRRADIATED_RESISTANCE.get());
        }
    }


}
