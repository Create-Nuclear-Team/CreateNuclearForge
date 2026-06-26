package net.nuclearteam.createnuclear;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nuclearteam.createnuclear.content.effects.IodineEffect;
import net.nuclearteam.createnuclear.content.radiation.RadiationEffect;

public class CNEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CreateNuclear.MOD_ID);

    public static final RegistryObject<MobEffect> RADIATION = EFFECTS.register("radiation", RadiationEffect::new);
    public static final RegistryObject<MobEffect> IODINE = EFFECTS.register("iodine", IodineEffect::new);

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
