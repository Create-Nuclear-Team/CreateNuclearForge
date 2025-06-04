package net.nuclearteam.createnuclear;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(bus = Bus.MOD)
public class CNCreativeModeTabs {
    private static final DeferredRegister<CreativeModeTab> REGISTER =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateNuclear.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = REGISTER.register("main",
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.createnuclear.main"))
            .icon(() -> CNItems.URANIUM_POWDER.asItem().getDefaultInstance())
            .displayItems((pPram, pOuts) -> {
                for (RegistryEntry<Item> entry : CreateNuclear.REGISTRATE.getAll(Registries.ITEM)) {
                    pOuts.accept(entry.get());
                }
            })
        ::build);


    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
