package net.nuclearteam.createnuclear.content.multiblock.reactorLogic;

import com.simibubi.create.content.logistics.BigItemStack;
import net.nuclearteam.createnuclear.api.multiblock.fluid.ReactorFluidType;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;

public interface IHeatCalculator {
    double computeHeat(BigItemStack bigFuelItem, BigItemStack bigCoolerItem, BigFluidStack bigFluidStack, ReactorFluidType type, int countGraphiteRod, int countUraniumRod, ReactorControllerInventory inventory, double overHeat);
}
