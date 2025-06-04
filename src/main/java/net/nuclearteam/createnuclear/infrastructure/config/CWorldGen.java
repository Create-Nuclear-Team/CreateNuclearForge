package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;

public class CWorldGen extends ConfigBase {
    public final ConfigBool disable = b(false, "disableWorldGen", Comments.disable);

    @Override
    public String getName() {
        return "worldGen";
    }

    private static class Comments {
        static String disable = "Prevents all worldgen added by Create from taking effect";
    }
}
