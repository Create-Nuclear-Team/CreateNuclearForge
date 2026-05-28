package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;

public class CNCCommon extends ConfigBase {
    public final CWorldGen worldGen = nested(0, CWorldGen::new, Comments.worldGen);
    public final CNReactor NUCLEAR = nested(0, CNReactor::new);

    @Override
    public String getName() {
        return "Common";
    }

    private static class Comments {
        static String worldGen = "Modify CreateNuclear's impact on your terrain";
        static String explode = "Modify the parameters of the explosion";
    }
}
