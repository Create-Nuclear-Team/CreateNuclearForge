package net.nuclearteam.createnuclear.content.multiblock.controller.service;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.api.multiblock.fluid.ReactorFluidType;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;

public interface IHeatService {
    int getGraphiteTimer();

    int getUraniumTimer();

    int getLiquidTimer();

    double calculateHeat(BigFluidStack bigFluidStack, int totalHeatRatio, ReactorControllerInventory inventory, Level level);
}
