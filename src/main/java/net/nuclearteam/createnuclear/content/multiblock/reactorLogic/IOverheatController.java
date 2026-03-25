package net.nuclearteam.createnuclear.content.multiblock.reactorLogic;

public interface IOverheatController {
    void updateState(int countGraphiteRod, int countUraniumRod);
    double getOverHeat();
    int getGraphiteTimer();
    int getUraniumTimer();
    int getLiquidTimer();
}
