package net.nuclearteam.createnuclear.content.multiblock.controller.service;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.api.multiblock.fluid.ReactorFluidType;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
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
    public  int getLiquidTimer() {
        return impl.getLiquidTimer();
    }

    @Override
    public double calculateHeat(BigItemStack fuel, BigItemStack cooler, BigFluidStack bigFluidStack, int graphiteCount, int uraniumCount, int totalHeatRatio, ReactorControllerInventory inventory, Level level) {
        return impl.calculateHeat(fuel, cooler, bigFluidStack, graphiteCount, uraniumCount, totalHeatRatio, inventory, level);
    }
}
