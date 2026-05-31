package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;
import net.minecraft.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public class CRods extends ConfigBase {
    public final ConfigInt maxHeat = i(1000, 200, 1000, "Maximum heat the reactor block can handle", Comments.maxHeat, Comments.hintHeat);
    public final ConfigInt rodFuelMaxForCoolerRod = i(3, 0, 20, "Maximum number of fuel rods allowed per cooled rod", Comments.warning, Comments.maxFuelPerCooled);

    public final ConfigInt uraniumRodLifetime = i(3600, 100, 5000, "Uranium rod lifetime (in ticks)", Comments.uraniumRodLifetime, Comments.hintTick);
    public final ConfigInt uraniumProxyBonus = i(32, -70, 70, "Heat bonus from nearby uranium rods", Comments.warning, Comments.uraniumProximityBonus);
    public final ConfigInt baseValueUranium = i(64, -70, 70, "Base heat value for uranium rods", Comments.warning, Comments.uraniumBaseValue);

    public final ConfigInt graphiteRodLifetime = i(3600, 100, 5000, "Graphite (cooling) rod lifetime (in ticks)", Comments.graphiteRodLifetime, Comments.hintTick);
    public final ConfigFloat graphiteProxyMalus = f((float) -1/4, -70, 70, "Heat penalty from nearby graphite rods", Comments.warning, Comments.graphiteProximityMalus);
    public final ConfigInt baseValueGraphite = i(-32, -50, 50, "Base heat value for graphite rods", Comments.warning, Comments.graphiteBaseValue);


    @Override
    public String getName() {
        return "Rods";
    }

    private static class Comments {
        static String hintTick = "20 ticks = 1 second";
        static String warning = "Modifying these values may unbalance reactor behavior";
        static String maxFuelPerCooled = "Maximum fuel rods supported by a single cooled rod";

        static String maxHeat = "Threshold heat above which the reactor may fail";
        static String hintHeat = "Used to avoid reactor failure due to excessive heat";

        static String uraniumRodLifetime = "Lifetime of uranium rods in the reactor";
        static String uraniumProximityBonus = "Bonus heat applied when uranium rods are adjacent";
        static String uraniumBaseValue = "Base heat contribution of a uranium rod before modifiers";

        static String graphiteRodLifetime = "Lifetime of graphite (cooling) rods in the reactor";
        static String graphiteProximityMalus = "Penalty to heat when graphite rods are adjacent";
        static String graphiteBaseValue = "Base heat contribution of a graphite rod before modifiers";

    }
}
