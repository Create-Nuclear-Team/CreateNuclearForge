package net.nuclearteam.createnuclear;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CNAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, CreateNuclear.MOD_ID);

    public static final RegistryObject<Attribute> IRRADIATED_RESISTANCE = ATTRIBUTES.register("generic.irradiated_resistance", () -> new RangedAttribute("attribute.name.createnuclear.generic.irradiated_resistance", 0, 0, 6));

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
}
