package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;

public class CNotify extends ConfigBase {

    public final ConfigBase.ConfigBool warnAllPlayers = b(false, "Warn all players", Comments.warning);
    public final ConfigBase.ConfigInt distanceOfWarning = i(100, 30, 5000, "Distance of warning", Comments.rangeOfWarning);

    @Override
    public String getName() {
        return "Notify";
    }

    private static class Comments {
        static String warning = "This is a warning before the reactor explodes";
        static String rangeOfWarning = "distance to check for players to warn of the imminent explosion";
    }
}
