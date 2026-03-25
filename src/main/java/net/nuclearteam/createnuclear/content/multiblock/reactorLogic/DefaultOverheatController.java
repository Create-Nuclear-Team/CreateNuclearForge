package net.nuclearteam.createnuclear.content.multiblock.reactorLogic;

class DefaultOverheatController implements IOverheatController {
    private int overFlowHeatTimer = 0;
    private int overFlowLimiter = 30;
    private double overHeat = 0;

    private final int maxUraniumPerGraphite = 3;
    private final int graphiteTimer = 3600;
    private final int uraniumTimer = 3600;
    private final int liquidTimer = 3600;

    @Override
    public void updateState(int countGraphiteRod, int countUraniumRod) {
        if (countGraphiteRod <= 0) return;

        if (countUraniumRod > countGraphiteRod * maxUraniumPerGraphite) {
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
    public int getGraphiteTimer() { return graphiteTimer; }

    @Override
    public int getUraniumTimer() { return uraniumTimer; }

    @Override
    public int getLiquidTimer()  { return liquidTimer; }
}
