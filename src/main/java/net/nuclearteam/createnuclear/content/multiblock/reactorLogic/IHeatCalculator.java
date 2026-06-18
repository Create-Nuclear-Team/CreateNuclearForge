package net.nuclearteam.createnuclear.content.multiblock.reactorLogic;

import net.nuclearteam.createnuclear.api.multiblock.fluid.ReactorFluidType;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;

public interface IHeatCalculator {
    double computeHeat(BigFluidStack bigFluidStack, ReactorFluidType type, ReactorControllerInventory inventory, double overHeat);
}
