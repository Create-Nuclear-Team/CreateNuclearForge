package net.nuclearteam.createnuclear.content.multiblock.reactorLogic;

import com.simibubi.create.content.logistics.BigItemStack;
import net.nuclearteam.createnuclear.api.ItemRodTypesValue;
import net.nuclearteam.createnuclear.api.multiblock.fluid.ReactorFluidType;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

/**
 * HeatManager facade delegating to extracted components (in separate files).
 */
public class HeatManager {
    private final IHeatCalculator calculator;
    private final IOverheatController overheatController;

    public HeatManager(IHeatCalculator calculator, IOverheatController overheatController) {
        this.calculator = calculator;
        this.overheatController = overheatController;
    }

    public HeatManager() {
        this(new DefaultHeatCalculator(), new DefaultOverheatController());
    }

    public double calculateHeat(BigItemStack bigFuelItem, BigItemStack bigCoolerItem, BigFluidStack bigFluidStack, ReactorFluidType type, int countGraphiteRod, int countUraniumRod, ReactorControllerInventory inventory) {
        if (bigFuelItem == null || bigCoolerItem == null) return 0;
        if (bigFuelItem.count <= 0 || bigCoolerItem.count <= 0) return 0;

        overheatController.updateState(countGraphiteRod, countUraniumRod);

        return calculator.computeHeat(bigFuelItem, bigCoolerItem, countGraphiteRod, countUraniumRod, inventory, overheatController.getOverHeat());
    }

    public int getGraphiteTimer() { return overheatController.getGraphiteTimer(); }
    public int getUraniumTimer() { return overheatController.getUraniumTimer(); }
}
