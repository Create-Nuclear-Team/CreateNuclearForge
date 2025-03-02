package net.nuclearteam.createnuclear;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.nuclearteam.createnuclear.foundation.ponder.CNPonderIndex;

public class CreateNuclearClient {

    public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEnventBus) {
        modEventBus.addListener(CreateNuclearClient::clientInit);

    }

    public static void clientInit(final FMLClientSetupEvent event) {
        CNPonderIndex.register();

    }
}
