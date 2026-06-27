package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;

public class CBiomeRestore  extends ConfigBase {
    public final ConfigInt maxCharge = i(16, 1, 64, "maxCharge", Comments.maxCharge);
    public final ConfigBool alwaysShowBar = b(false, "alwaysShowBar", Comments.alwaysShowBar);
    public final ConfigBool restoreInCircle = b(false, "restoreInCircle", Comments.restoreInCircle);
    public final ConfigInt restoreRadiusChunks = i(1, 1, 3, "restoreRadiusChunks", Comments.restoreRadiusChunks);

    @Override
    public String getName() {
        return "BiomeRestore";
    }

    private static class Comments {
        static String maxCharge = "Maximum number of charges the Biome Restore Cell can hold.";
        static String alwaysShowBar = "If true, the durability-style bar is always shown, even when the cell is empty. "
                + "If false, the bar is hidden when the charge is 0.";
        static String restoreInCircle = "If true, restoring a chunk also restores nearby chunks in a circle "
                + "(see restoreRadiusChunks). If false, only the chunk the player stands in is restored.";
        static String restoreRadiusChunks = "Radius in chunks used when restoreInCircle is true (1 to 3).";
    }
}
