package net.nuclearteam.createnuclear.impl.registry;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.nuclearteam.createnuclear.api.CreateNuclearRegistries;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType;
import org.jetbrains.annotations.ApiStatus.Internal;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateNuclearRegistriesImpl {
    @Internal
    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                CreateNuclearRegistries.ROD_TYPE,
                RodType.CODEC,
                RodType.CODEC
        );
    }
}
