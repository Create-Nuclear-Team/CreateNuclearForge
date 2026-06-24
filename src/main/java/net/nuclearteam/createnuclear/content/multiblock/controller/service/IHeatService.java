package net.nuclearteam.createnuclear.content.multiblock.controller.service;

import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;
import net.nuclearteam.createnuclear.content.multiblock.controller.display.ReactorDisplayState;

public interface IHeatService {
    int getLiquidTimer();

    double calculateHeat(BigFluidStack bigFluidStack, int totalHeatRatio, ReactorControllerInventory inventory, Level level, ReactorDisplayState displayState);
}
