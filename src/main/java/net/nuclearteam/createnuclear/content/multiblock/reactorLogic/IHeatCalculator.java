package net.nuclearteam.createnuclear.content.multiblock.reactorLogic;

import com.simibubi.create.content.logistics.BigItemStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;

public interface IHeatCalculator {
    double computeHeat(BigItemStack bigFuelItem, BigItemStack bigCoolerItem, int countGraphiteRod, int countUraniumRod, ReactorControllerInventory inventory, double overHeat);
}
