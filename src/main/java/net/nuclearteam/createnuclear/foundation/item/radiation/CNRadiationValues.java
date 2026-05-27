package net.nuclearteam.createnuclear.foundation.item.radiation;

import com.simibubi.create.AllItems;
import net.nuclearteam.createnuclear.api.radiation.RadiationRegistry;

public class CNRadiationValues {
    public static void register() {
        RadiationRegistry.register()
            .item(AllItems.CRUSHED_URANIUM.asItem())
            .value(.5D)
            .build();
    }
}
