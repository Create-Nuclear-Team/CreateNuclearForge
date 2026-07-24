package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;
import net.minecraft.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public class CRods extends ConfigBase {

    public final ConfigInt uraniumRodLifetime = i(3600, 100, 5000, "uranium_rod_lifetime_ticks", Comments.uraniumRodLifetime, Comments.hintTick);
    public final ConfigInt uraniumProximityBonus = i(32, -70, 70, "uranium_proximity_bonus", Comments.warning, Comments.uraniumProximityBonus);
    public final ConfigInt uraniumBaseValue = i(64, -70, 70, "uranium_base_value", Comments.warning, Comments.uraniumBaseValue);

    public final ConfigInt graphiteRodLifetime = i(3600, 100, 5000, "graphite_rod_lifetime_ticks", Comments.graphiteRodLifetime, Comments.hintTick);
    public final ConfigFloat graphiteProximityMalus = f(-0.25f, -70, 70, "graphite_proximity_penalty", Comments.warning, Comments.graphiteProximityMalus);
    public final ConfigInt graphiteBaseValue = i(-32, -50, 50, "graphite_base_value", Comments.warning, Comments.graphiteBaseValue);

    @Override
    public String getName() {
        return "rods";
    }

    private static class Comments {
        static String hintTick = "20 ticks = 1 second";
        static String warning = "Changing these values may unbalance reactor behavior.";
        static String maxFuelPerCooled = "Maximum fuel rods supported by a single cooled rod";

        static String uraniumRodLifetime = "Lifetime of uranium rods in ticks.";
        static String uraniumProximityBonus = "Heat bonus applied when uranium rods are adjacent.";
        static String uraniumBaseValue = "Base heat contribution of a uranium rod before modifiers.";

        static String graphiteRodLifetime = "Lifetime of graphite (cooling) rods in ticks.";
        static String graphiteProximityMalus = "Heat penalty applied when graphite rods are adjacent.";
        static String graphiteBaseValue = "Base heat contribution of a graphite rod before modifiers.";
    }
}