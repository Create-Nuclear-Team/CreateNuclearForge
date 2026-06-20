package net.nuclearteam.createnuclear.content.multiblock.controller.service;

import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;
import net.nuclearteam.createnuclear.content.multiblock.reactorLogic.HeatManager;

public class DefaultHeatService implements IHeatService {
    private final HeatManager impl;

    public DefaultHeatService(HeatManager impl) {
        this.impl = impl;
    }
    
    @Override
    public  int getLiquidTimer() {
        return impl.getLiquidTimer();
    }

    @Override
    public double calculateHeat(BigFluidStack bigFluidStack, int totalHeatRatio, ReactorControllerInventory inventory, Level level) {
        return impl.calculateHeat(bigFluidStack, totalHeatRatio, inventory, level);
    }
}
