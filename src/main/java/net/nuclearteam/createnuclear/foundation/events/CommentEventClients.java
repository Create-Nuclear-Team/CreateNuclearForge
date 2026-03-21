package net.nuclearteam.createnuclear.foundation.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import net.nuclearteam.createnuclear.CNEntityType;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.foundation.item.RodsStats;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class CommentEventClients {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        CNEntityType.registerModelLayer(event);
    }

    @EventBusSubscriber(modid = CreateNuclear.MOD_ID, value = Dist.CLIENT, bus = Bus.FORGE)
    public static class RodsTooltipHandler {

        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            Item item = stack.getItem();
            Player player = event.getEntity();

            if (player == null) return;

            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id != null && CreateNuclear.MOD_ID.equals(id.getNamespace())) return;

            RodsStats rodsStats = RodsStats.create(item);
            rodsStats.modify(event);
        }

    }
}
