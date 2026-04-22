package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;
import net.minecraft.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public class CRadiation extends ConfigBase {
    public final ConfigBool enabledItemRadiation = b(true, "enabledItemRadiation", Comments.enabled);

    @Override
    public String getName() {
        return "Radiation";
    }

    private static class Comments {
        static String enabled = "When enabled, certain items may emit radiation that affects the player and nearby entities. "
            + "Disable this to neutralize radiation effects without removing the items.";
    }
}
