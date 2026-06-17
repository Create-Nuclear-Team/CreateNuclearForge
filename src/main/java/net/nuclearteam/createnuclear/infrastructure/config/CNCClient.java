package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;
import net.nuclearteam.createnuclear.CNParticleTypes;

public class CNCClient extends ConfigBase {
    public final ConfigBool nuclearBombFlash = b(true, "nuclearBombFlash", Comments.nuclearBombFlash);
    public ConfigBool screenShaking = b(true, "screenShaking", Comments.screenShaking);

    @Override
    public String getName() { return "Client"; }

    private static class Comments {
        static String nuclearBombFlash = "Enables the flash at the start of the nuclear explosion.";
        static String screenShaking = "Enables the tremor during the nuclear explosion";
    }
}
