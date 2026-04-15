package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;

public class CNCClient extends ConfigBase {
    public final ConfigBool nuclearBombFlash = b(true, "nuclearBombFlash", Comments.nuclearBombFlash);

    @Override
    public String getName() { return "Client"; }

    private static class Comments {
        static String nuclearBombFlash = "Modify the duration and configuration of rods.";
    }
}
