package net.nuclearteam.createnuclear;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.nuclearteam.createnuclear.content.particles.NuclearMushroomCloudParticle;
import net.nuclearteam.createnuclear.content.particles.SmallNuclearExplosionParticle;
import net.nuclearteam.createnuclear.foundation.ponder.CreateNuclearPonderPlugin;


@SuppressWarnings("unused")
public class CreateNuclearClient extends CNClientProxy {

    public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(CreateNuclearClient::clientInit);
        modEventBus.addListener(CNParticleTypes::registerFactories);

    }

    public static void clientInit(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CreateNuclearPonderPlugin());
    }

    @Override
    public void commonInit() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setupParticles);
    }

    public void setupParticles(RegisterParticleProvidersEvent registry) {
        CreateNuclear.LOGGER.debug("Registered particle factories");
        registry.registerSpecial(CNParticleRegistry.NUCLEAR_MUSHROOM_CLOUD.get(), new NuclearMushroomCloudParticle.Factory());
        registry.registerSpriteSet(CNParticleRegistry.NUCLEAR_MUSHROOM_CLOUD_SMOKE.get(), SmallNuclearExplosionParticle.NukeFactory::new);
        registry.registerSpriteSet(CNParticleRegistry.NUCLEAR_MUSHROOM_CLOUD_EXPLOSION.get(), SmallNuclearExplosionParticle.NukeFactory::new);
    }
}
