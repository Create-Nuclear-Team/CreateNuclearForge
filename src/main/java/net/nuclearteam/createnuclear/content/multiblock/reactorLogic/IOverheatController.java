package net.nuclearteam.createnuclear.content.multiblock.reactorLogic;

import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;

public interface IOverheatController {
    void updateState(int countGraphiteRod, int countUraniumRod, BigFluidStack bigFluidStack);
    double getOverHeat();
    int getGraphiteTimer();
    int getUraniumTimer();
    int getLiquidTimer();
}
