package net.nuclearteam.createnuclear.impl.registry;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.nuclearteam.createnuclear.api.CreateNuclearRegistries;
import net.nuclearteam.createnuclear.api.multiblock.fluid.ReactorFluidType;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType;
import org.jetbrains.annotations.ApiStatus.Internal;

@EventBusSubscriber(bus = Bus.MOD)
public class CreateNuclearRegistriesImpl {
    @Internal
    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                CreateNuclearRegistries.ROD_TYPE,
                RodType.CODEC,
                RodType.CODEC
        );

        event.dataPackRegistry(
                CreateNuclearRegistries.FLUID_TYPE,
                ReactorFluidType.CODEC,
                ReactorFluidType.CODEC
        );
    }
}
