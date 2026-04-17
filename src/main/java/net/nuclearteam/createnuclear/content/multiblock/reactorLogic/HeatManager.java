package net.nuclearteam.createnuclear.content.multiblock.reactorLogic;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.world.level.Level;
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

    public double calculateHeat(BigItemStack fuel, BigItemStack cooler, BigFluidStack bigFluidStack, int graphiteCount, int uraniumCount, ReactorControllerInventory inventory, Level level) {
        if (fuel == null || cooler == null) return 0;
        if (fuel.count <= 0 || cooler.count <= 0) return 0;

        overheatController.updateState(graphiteCount, uraniumCount, bigFluidStack);
        ReactorFluidType type = bigFluidStack == null ? null : bigFluidStack.getFluidtype(level);

        return calculator.computeHeat(fuel, cooler, bigFluidStack, type, graphiteCount, uraniumCount, inventory, overheatController.getOverHeat());
    }

    public int getGraphiteTimer() { return overheatController.getGraphiteTimer(); }
    public int getUraniumTimer() { return overheatController.getUraniumTimer(); }
    public int getLiquidTimer() { return  overheatController.getLiquidTimer();}
}
