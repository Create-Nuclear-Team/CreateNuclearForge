package net.nuclearteam.createnuclear.content.multiblock.controller.service;

import com.simibubi.create.content.logistics.BigItemStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;
import net.nuclearteam.createnuclear.content.multiblock.reactorLogic.HeatManager;

public class DefaultHeatService implements IHeatService {
    private final HeatManager impl;

    public DefaultHeatService(HeatManager impl) {
        this.impl = impl;
    }
    
    @Override
    public int getGraphiteTimer() {
        return impl.getGraphiteTimer();
    }
    
    @Override
    public int getUraniumTimer() {
        return impl.getUraniumTimer();
    }

    @Override
    public double calculateHeat(BigItemStack fuel, BigItemStack cooler, int graphiteCount, int uraniumCount, ReactorControllerInventory inventory) {
        return impl.calculateHeat(fuel, cooler, graphiteCount, uraniumCount, inventory);
    }
}
