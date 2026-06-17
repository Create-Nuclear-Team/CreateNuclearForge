package net.nuclearteam.createnuclear.foundation.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.foundation.item.RodsStats;

@Mod.EventBusSubscriber(modid = CreateNuclear.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RodsTooltipHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        Player player = event.getEntity();

        if (player == null) return;

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        // Mod items already get their rod tooltip via Registrate's setTooltipModifierFactory
        // (CreateNuclear.java:48). This handler only serves EXTERNAL items (other mods or
        // datapack-defined RodTypes resolved at runtime via RodType.resolveRodType), which
        // setTooltipModifierFactory cannot cover. Do NOT remove/invert: doing so double-tooltips mod rods.
        if (id != null && CreateNuclear.MOD_ID.equals(id.getNamespace())) return;

        RodsStats rodsStats = RodsStats.create(item);
        rodsStats.modify(event);
    }
}
