package net.nuclearteam.createnuclear;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import net.nuclearteam.createnuclear.impl.effect.RadiationEffetcHandler;

public class CNOpenPipeEffectHandlers {
    public static void registerDefaults() {
        OpenPipeEffectHandler.REGISTRY.register(CNFluids.URANIUM.getSource(), new RadiationEffetcHandler());
    }
}
