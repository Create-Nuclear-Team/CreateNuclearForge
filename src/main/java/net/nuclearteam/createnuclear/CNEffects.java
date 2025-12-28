package net.nuclearteam.createnuclear;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nuclearteam.createnuclear.content.effects.IrradiationCurseEffect;
import net.nuclearteam.createnuclear.content.effects.RadiationEffect;

public class CNEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CreateNuclear.MOD_ID);

    public static final RegistryObject<MobEffect> RADIATION = EFFECTS.register("radiation", RadiationEffect::new);
    public static final RegistryObject<MobEffect> TEST = EFFECTS.register("curse", () -> new IrradiationCurseEffect(MobEffectCategory.HARMFUL, 0xAE1A1A));

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
