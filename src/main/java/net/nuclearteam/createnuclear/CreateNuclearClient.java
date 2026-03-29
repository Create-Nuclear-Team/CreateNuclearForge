package net.nuclearteam.createnuclear;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.nuclearteam.createnuclear.foundation.ponder.CreateNuclearPonderPlugin;


@SuppressWarnings("unused")
public class CreateNuclearClient {

    public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(CreateNuclearClient::clientInit);

    }

    public static void clientInit(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CreateNuclearPonderPlugin());
        ItemProperties.register(CNItems.ANTI_RADIATION_HELMETS.asItem(), CreateNuclear.asResource("cloth"),
            (itemStack, clientLevel, livingEntity, i) -> {
                CompoundTag tag = itemStack.getOrCreateTag();
                CreateNuclear.LOGGER.warn("ItemProperties::register tag: {}, {}", tag.toString(), itemStack);
                if (!tag.contains("Cloth")) return 0f;

                return switch (tag.getString("Cloth")){
                    case "black" -> 1f;
                    case "blue" -> 2f;
                    default -> 0f;
                };

            }
        );
    }
}
