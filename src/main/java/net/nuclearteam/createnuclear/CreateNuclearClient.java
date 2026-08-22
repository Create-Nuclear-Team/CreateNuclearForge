package net.nuclearteam.createnuclear;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.nuclearteam.createnuclear.content.particles.NuclearMushroomCloudParticle;
import net.nuclearteam.createnuclear.content.particles.SmallNuclearExplosionParticle;
import net.nuclearteam.createnuclear.content.biome.BiomeIrradiationExtractorItem;
import net.nuclearteam.createnuclear.foundation.ponder.CreateNuclearPonderPlugin;
import net.nuclearteam.createnuclear.foundation.utility.ClothTagHelper;

@SuppressWarnings("unused")
public class CreateNuclearClient {

    public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(CreateNuclearClient::clientInit);
        modEventBus.addListener(CreateNuclearClient::setupParticles);
        modEventBus.addListener(CNParticleTypes::registerFactories);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CreateNuclearPonderPlugin());
        event.enqueueWork(CreateNuclearClient::registerItemProperties);
    }


    private static void registerItemProperties() {
        clothItemProperties();
        biomeRestoreItemProperties();
    }

    /**
     * Registers the {@code createnuclear:cloth_color} item property used by the anti-radiation
     * armor item models to pick the right colored icon based on the {@code ClothColor} NBT tag.
     * <p>
     * {@link ItemProperties#register(Item, ResourceLocation, ItemPropertyFunction)} clamps the value to {@code [0,1]}, so the dyed color id is
     * normalized to {@code (id+1)/16} (range {@code 0.0625..1.0}) and undyed maps to {@code 0} —
     * below the smallest override threshold, so no override fires and the default icon shows.
     * The model overrides in {@code CNItems.coloredArmorModel} use the matching {@code (id+1)/16}
     * predicate values. Mirrors the dyed worn-armor texture selected by
     * {@code AntiRadiationArmorItem.getArmorTexture}.
     */
    private static void clothItemProperties() {
        ResourceLocation clothColor = CreateNuclear.asResource("cloth_color");
        ClampedItemPropertyFunction fn = (stack, level, entity, seed) -> {
            DyeColor dye = DyeColor.byName(ClothTagHelper.getClothColor(stack, "default"), null);
            return dye == null ? 0f : (dye.getId() + 1) / 16f;
        };
        ItemProperties.register(CNItems.ANTI_RADIATION_HELMETS.get(), clothColor, fn);
        ItemProperties.register(CNItems.ANTI_RADIATION_CHESTPLATES.get(), clothColor, fn);
        ItemProperties.register(CNItems.ANTI_RADIATION_LEGGINGS.get(), clothColor, fn);
        ItemProperties.register(CNItems.ANTI_RADIATION_BOOTS.get(), clothColor, fn);
    }

    /**
     * Registers the {@code createnuclear:charge} item property used by the biome restore cell's
     * model to pick a texture variant based on its current charge ratio (0.0 = empty, 1.0 = full).
     */
    private static void biomeRestoreItemProperties() {
        ResourceLocation biomeRestore = CreateNuclear.asResource(BiomeIrradiationExtractorItem.TAG);
        ClampedItemPropertyFunction fn = ((stack, level, entity, seed) -> {
            int max = BiomeIrradiationExtractorItem.getMaxCharge();

            return (float) BiomeIrradiationExtractorItem.getChargeTag(stack, 0) / max;
        });

        ItemProperties.register(CNItems.IRRADIATION_BIOME_EXTRACTOR.get(), biomeRestore, fn);
    }

    public static void setupParticles(RegisterParticleProvidersEvent registry) {
        CreateNuclear.LOGGER.debug("Registered particle factories");
        registry.registerSpecial(CNParticleRegistry.NUCLEAR_MUSHROOM_CLOUD.get(), new NuclearMushroomCloudParticle.Factory());
        registry.registerSpriteSet(CNParticleRegistry.NUCLEAR_MUSHROOM_CLOUD_SMOKE.get(), SmallNuclearExplosionParticle.NukeFactory::new);
        registry.registerSpriteSet(CNParticleRegistry.NUCLEAR_MUSHROOM_CLOUD_EXPLOSION.get(), SmallNuclearExplosionParticle.NukeFactory::new);
    }
}
