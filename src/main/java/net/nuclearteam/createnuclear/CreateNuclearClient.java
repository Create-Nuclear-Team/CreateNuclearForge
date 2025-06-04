package net.nuclearteam.createnuclear;

import net.createmod.ponder.foundation.PonderIndex;
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

    }
}
