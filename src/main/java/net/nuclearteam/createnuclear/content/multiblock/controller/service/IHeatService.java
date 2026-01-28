package net.nuclearteam.createnuclear.content.multiblock.controller.service;

import com.simibubi.create.content.logistics.BigItemStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;

public interface IHeatService {
    int getGraphiteTimer();
    int getUraniumTimer();
    double calculateHeat(BigItemStack fuel, BigItemStack cooler, int graphiteCount, int uraniumCount, ReactorControllerInventory inventory);
}
