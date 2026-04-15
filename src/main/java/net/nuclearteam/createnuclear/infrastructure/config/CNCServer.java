package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;

public class CNCServer extends ConfigBase {
    public final CRods rods = nested(0, CRods::new, Comments.rods);
    public final CExplode explode = nested(0, CExplode::new, Comments.explode);
    public CNotify notify = nested(50, CNotify::new,"ratio");

    @Override
    public String getName() {
        return "Server";
    }

    private static class Comments {
        static String rods = "Modify the duration and configuration of rods.";
        static String explode = "Explosion settings.";
    }
}
