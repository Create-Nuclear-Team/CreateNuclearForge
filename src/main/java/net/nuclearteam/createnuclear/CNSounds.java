package net.nuclearteam.createnuclear;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CNSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister
            .create(ForgeRegistries.SOUND_EVENTS, CreateNuclear.MOD_ID);

    public static final RegistryObject<SoundEvent> REACTOR_CASING_BREAK = registerSoundEvent("reactor_casing_break");
    public static final RegistryObject<SoundEvent> REACTOR_CASING_STEP = registerSoundEvent("reactor_casing_step");
    public static final RegistryObject<SoundEvent> REACTOR_CASING_PLACE = registerSoundEvent("reactor_casing_place");
    public static final RegistryObject<SoundEvent> REACTOR_CASING_HIT = registerSoundEvent("reactor_casing_hit");
    public static final RegistryObject<SoundEvent> REACTOR_CASING_FALL = registerSoundEvent("reactor_casing_fall");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CreateNuclear.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    public static net.minecraft.world.level.block.SoundType getSoundType(String id) {
        return new net.minecraft.world.level.block.SoundType(1.0F, 1.0F,
                SoundEvent.createVariableRangeEvent(new ResourceLocation(CreateNuclear.MOD_ID, id + "_break")),
                SoundEvent.createVariableRangeEvent(new ResourceLocation(CreateNuclear.MOD_ID, id + "_step")),
                SoundEvent.createVariableRangeEvent(new ResourceLocation(CreateNuclear.MOD_ID, id + "_place")),
                SoundEvent.createVariableRangeEvent(new ResourceLocation(CreateNuclear.MOD_ID, id + "_hit")),
                SoundEvent.createVariableRangeEvent(new ResourceLocation(CreateNuclear.MOD_ID, id + "_fall")));
    }
}
