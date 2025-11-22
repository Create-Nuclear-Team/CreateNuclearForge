package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;

public class CNReactor extends ConfigBase {
    public final ConfigBool nuclear_explosion_daemon_thread = b(false, "C'est un test de daemond");

    @Override
    public String getName() {
        return "Test";
    }
}
