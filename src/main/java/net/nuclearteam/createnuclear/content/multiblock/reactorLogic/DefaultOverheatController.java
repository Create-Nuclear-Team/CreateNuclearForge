package net.nuclearteam.createnuclear.content.multiblock.reactorLogic;

import net.nuclearteam.createnuclear.api.multiblock.fluid.ReactorFluidType;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;

class DefaultOverheatController implements IOverheatController {
    private int overFlowHeatTimer = 0;
    private int overFlowLimiter = 30;
    private double overHeat = 0;

    private final int liquidTimer = 3600;

    @Override
    public void updateState(int totalHeatRatio, BigFluidStack bigFluidStack, ReactorFluidType type) {
        int fluidAmount = bigFluidStack == null ? 0 : bigFluidStack.amount;
        int fluidEfficiency = type == null ? -1 : type.efficiency();
        if (totalHeatRatio > 0 || fluidEfficiency == -1 || fluidAmount < fluidEfficiency) {
            overFlowHeatTimer++;
            if (overFlowHeatTimer >= overFlowLimiter) {
                overHeat += 1;
                overFlowHeatTimer = 0;
                if (overFlowLimiter > 2) overFlowLimiter -= 1;
            }
        } else {
            overFlowHeatTimer = 0;
            overFlowLimiter = 30;
            if (overHeat > 0) overHeat -= 2;
            else overHeat = 0;
        }
    }

    @Override
    public double getOverHeat() { return overHeat; }


    @Override
    public int getLiquidTimer()  { return liquidTimer; }
}
