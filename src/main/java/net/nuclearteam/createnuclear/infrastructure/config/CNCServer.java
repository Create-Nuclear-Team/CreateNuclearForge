package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;

public class CNCServer extends ConfigBase {
    public final CNotify notify = nested(0, CNotify::new, CNCServer.Comments.notify);

    @Override
    public String getName() {
        return "";
    }

    private static class Comments {
        static String notify = "Manage the notification before the explosion";
    }
}
