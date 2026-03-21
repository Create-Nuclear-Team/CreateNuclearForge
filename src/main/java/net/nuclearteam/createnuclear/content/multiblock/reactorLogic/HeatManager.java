package net.nuclearteam.createnuclear.content.multiblock.reactorLogic;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.api.ItemRodTypesValue;
import net.nuclearteam.createnuclear.api.multiblock.fluid.ReactorFluidType;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

public class HeatManager {
    public int heat;
    int overFlowHeatTimer = 0;
    int overFlowLimiter = 30;
    double overHeat = 0;
    public int baseUraniumHeat = CNConfigs.server().rods.baseValueUranium.get();
    public int baseGraphiteHeat = CNConfigs.server().rods.baseValueGraphite.get();
    public int proximityUraniumHeat = CNConfigs.server().rods.uraniumProxyBonus.get();
    public int proximityGraphiteHeat = CNConfigs.server().rods.graphiteProxyMalus.get();
    public int maxUraniumPerGraphite = CNConfigs.server().rods.rodFuelMaxForCoolerRod.get();
    public int graphiteTimer = 3600;
    public int uraniumTimer = 3600;

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

    public double calculateHeat(BigItemStack bigFuelItem, BigItemStack bigCoolerItem, BigFluidStack bigFluidStack, ReactorFluidType type, int countGraphiteRod, int countUraniumRod, ReactorControllerInventory inventory) {
        heat = 0;

        if (bigFuelItem.count <= 0 || bigCoolerItem.count <= 0) {
            return 0;
        }

        updateOverheatState(countGraphiteRod, countUraniumRod);

        // the offsets for the four directions (down, up, right, left) is int[][] offsets = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} }; (defined at the top of the class)
        String currentRod = "";
        ListTag list = inventory.getStackInSlot(0).getOrCreateTag().getCompound("pattern").getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack stackCar = ItemStack.of(list.getCompound(i));
            RodType rod = ItemRodTypesValue.getRodType(stackCar.getItem());
            if (stackCar.is(CNTags.CNItemTags.FUEL.tag) || (rod.items().size() > 0 && rod.type() == RodType.TypeRod.FUEL)) {
                heat += baseUraniumHeat;
                currentRod = "u";
            } else if (stackCar.is(CNTags.CNItemTags.COOLER.tag)) {
                heat += baseGraphiteHeat;
                currentRod = "g";
            }
            for (int j = 0; j < formattedPattern.length; j++) {
                for (int k = 0; k < formattedPattern[j].length; k++) {
                    // Skip if the current pattern value is 99
                    if (formattedPattern[j][k] == 99) continue;

                    // Check if the current slot matches the pattern
                    if (list.getCompound(i).getInt("Slot") != formattedPattern[j][k]) continue;

                    // For each neighbor (up, down, right, left)
                    for (int[] offset : offsets) {
                        int nj = j + offset[0];
                        int nk = k + offset[1];

                        // Check if the indices are within the array boundaries
                        if (nj < 0 || nj >= formattedPattern.length || nk < 0 || nk >= formattedPattern[j].length)
                            continue;

                        int neighborSlot = formattedPattern[nj][nk];

                        // Loop through the list to find the neighbor slot
                        for (int l = 0; l < list.size(); l++) {
                            if (list.getCompound(l).getInt("Slot") == neighborSlot) {
                                // If the currentRod equals "u", apply the corresponding heat
                                if (currentRod.equals("u")) {
                                    ItemStack stack = ItemStack.of(list.getCompound(i));
                                    if (stack.is(CNTags.CNItemTags.FUEL.tag)) {
                                        heat += proximityUraniumHeat;
                                    } else if (stack.is(CNTags.CNItemTags.COOLER.tag)) {
                                        heat += proximityGraphiteHeat;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        double heatCaculed = heat + overHeat;

        if (heatCaculed > type.maxHeat() && bigFluidStack.amount > type.efficiency()) {
            heat = 9_999_999;
        }

        return heat;
    }

    private void updateOverheatState(int countGraphiteRod, int countUraniumRod) {
        // if more than maxUraniumPerGraphite of the rods are uranium, the reactor will overheat
        if (countUraniumRod > countGraphiteRod * maxUraniumPerGraphite) {
            overFlowHeatTimer++;
            if (overFlowHeatTimer >= overFlowLimiter) {
                overHeat+=1;
                overFlowHeatTimer= 0;
                if (overFlowLimiter > 2) {
                    overFlowLimiter -= 1;
                }
            }
        } else {
            overFlowHeatTimer = 0;
            overFlowLimiter = 30;
            if (overHeat > 0) {
                overHeat -= 2;
            } else {
                overHeat = 0;
            }
        }
    }
}
