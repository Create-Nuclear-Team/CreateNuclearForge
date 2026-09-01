package net.nuclearteam.createnuclear.content.redstone.displayLink.source;

import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

final class ReactorDisplayConstants {
    static final int MAX_FUEL = 64;
    static final int MAX_COOLER = 64;
    static final int MAX_FLUID = 16000;
    static final int MAX_HEAT = 1000;
    private ReactorDisplayConstants() {}

    public static int maxHeatForSize(int size) {
        return switch (size) {
            case 7 -> CNConfigs.server().reactorHeat.size7Danger.get();
            case 9 -> CNConfigs.server().reactorHeat.size9Danger.get();
            default -> CNConfigs.server().reactorHeat.size5Danger.get();
        };
    }
}
