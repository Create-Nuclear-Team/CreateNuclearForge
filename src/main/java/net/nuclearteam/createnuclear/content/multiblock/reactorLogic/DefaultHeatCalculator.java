package net.nuclearteam.createnuclear.content.multiblock.reactorLogic;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.nuclearteam.createnuclear.api.multiblock.fluid.ReactorFluidType;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType.TypeRod;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType.TypeRodPredicate;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.ReactorBluePrintItem;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;
import net.nuclearteam.createnuclear.content.multiblock.controller.display.ReactorDisplayState;

import java.util.HashMap;
import java.util.Map;

public class DefaultHeatCalculator implements IHeatCalculator {
    private final int[][] formattedPattern = new int[][]{
            {99,99,99,0,1,2,99,99,99},
            {99,99,3,4,5,6,7,99,99},
            {99,8,9,10,11,12,13,14,99},
            {15,16,17,18,19,20,21,22,23},
            {24,25,26,27,28,29,30,31,32},
            {33,34,35,36,37,38,39,40,41},
            {99,42,43,44,45,46,47,48,99},
            {99,99,49,50,51,52,53,99,99},
            {99,99,99,54,55,56,99,99,99}
    };
    private final int[][] offsets = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };

    @Override
    public double computeHeat(BigFluidStack bigFluidStack, ReactorFluidType type, ReactorControllerInventory inventory, double overHeat, ReactorDisplayState displayState, Level level) {
        double heat = 0;

        ItemStackHandler pattern = ReactorBluePrintItem.getItemStorage(inventory.getStackInSlot(0));

        Map<Item, Integer> availableItems = displayState != null && displayState.items() != null 
                ? new HashMap<>(displayState.items()) 
                : new HashMap<>();

        Map<Integer, ItemStack> actualRods = new HashMap<>();

        for (int slot = 0; slot < pattern.getSlots(); slot++) {
            ItemStack currentStack = pattern.getStackInSlot(slot);
            if (currentStack.isEmpty()) continue;

            Item rodItem = currentStack.getItem();
            if (availableItems.getOrDefault(rodItem, 0) > 0) {
                availableItems.put(rodItem, availableItems.get(rodItem) - 1);
                actualRods.put(slot, currentStack);
            }
        }

        for (Map.Entry<Integer, ItemStack> entry : actualRods.entrySet()) {
            int slot = entry.getKey();
            ItemStack currentStack = entry.getValue();
            RodType rod = RodType.resolveRodType(currentStack.getItem(), level);

            if (!rod.isNotEmptyItem() || rod.type() == TypeRod.NONE) continue;

            heat += rod.baseRodHeat().get();

            // find position in formattedPattern and check neighbors
            for (int j = 0; j < formattedPattern.length; j++) {
                for (int k = 0; k < formattedPattern[j].length; k++) {
                    if (slot != formattedPattern[j][k]) continue;

                    for (int[] offset : offsets) {
                        int nj = j + offset[0];
                        int nk = k + offset[1];
                        if (nj < 0 || nj >= formattedPattern.length || nk < 0 || nk >= formattedPattern[j].length) continue;

                        ItemStack neighborStack = actualRods.get(formattedPattern[nj][nk]);
                        if (neighborStack == null) continue;

                        RodType neighborRod = RodType.resolveRodType(neighborStack.getItem(), level);
                        if (!neighborRod.isNotEmptyItem()) continue;

                        if (TypeRodPredicate.isFuel(rod) && TypeRodPredicate.isFuel(neighborRod)) {
                            heat += rod.proximityRodHeat().get();
                        } else if (TypeRodPredicate.isCooled(rod) && TypeRodPredicate.isCooled(neighborRod)) {
                            heat += neighborRod.baseRodHeat().get() * rod.proximityRodHeat().get();
                        }
                    }
                }
            }
        }
        return Math.max(0, heat + overHeat);
    }
}
