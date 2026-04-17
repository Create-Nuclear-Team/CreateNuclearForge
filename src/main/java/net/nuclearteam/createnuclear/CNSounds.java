package net.nuclearteam.createnuclear;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.block.SoundType;

public class CNSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister
            .create(ForgeRegistries.SOUND_EVENTS, CreateNuclear.MOD_ID);

    public static final RegistryObject<SoundEvent> REACTOR_CASING_BREAK = registerSoundEvent("reactor_casing_break");
    public static final RegistryObject<SoundEvent> REACTOR_CASING_STEP = registerSoundEvent("reactor_casing_step");
    public static final RegistryObject<SoundEvent> REACTOR_CASING_PLACE = registerSoundEvent("reactor_casing_place");
    public static final RegistryObject<SoundEvent> REACTOR_CASING_HIT = registerSoundEvent("reactor_casing_hit");
    public static final RegistryObject<SoundEvent> REACTOR_CASING_FALL = registerSoundEvent("reactor_casing_fall");
    public static  final RegistryObject<SoundEvent> LARGE_NUCLEAR_EXPLOSION = registerSoundEvent("large_nuclear_explosion");
    public static  final RegistryObject<SoundEvent> NUCLEAR_EXPLOSION = registerSoundEvent("nuclear_explosion");
    public static  final RegistryObject<SoundEvent> NUCLEAR_EXPLOSION_RINGING = registerSoundEvent("nuclear_explosion_ringing");
    public static  final RegistryObject<SoundEvent> NUCLEAR_EXPLOSION_RUMBLE = registerSoundEvent("nuclear_explosion_rumble");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(CreateNuclear.asResource(name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    public static SoundType getSoundType(String id) {
        return new ForgeSoundType(1.0F, 1.0F,
                () -> SoundEvent.createVariableRangeEvent(CreateNuclear.asResource(id + "_break")),
                () -> SoundEvent.createVariableRangeEvent(CreateNuclear.asResource(id + "_step")),
                () -> SoundEvent.createVariableRangeEvent(CreateNuclear.asResource(id + "_place")),
                () -> SoundEvent.createVariableRangeEvent(CreateNuclear.asResource(id + "_hit")),
                () -> SoundEvent.createVariableRangeEvent(CreateNuclear.asResource(id + "_fall")));
    }
}
