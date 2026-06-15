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
    public double computeHeat(BigItemStack bigFuelItem, BigItemStack bigCoolerItem, BigFluidStack bigFluidStack, ReactorFluidType type, int countGraphiteRod, int countUraniumRod, ReactorControllerInventory inventory, double overHeat) {
        double heat = 0;

        ListTag list = inventory.getStackInSlot(0).getOrCreateTag().getCompound("pattern").getList("Items", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            ItemStack currentStack = ItemStack.of(list.getCompound(i));
            RodType rod = ItemRodTypesValue.getRodType(currentStack.getItem());
            String currentRod = "";
            if (currentStack.is(CNTags.CNItemTags.FUEL.tag) || (rod.items().size() > 0 && rod.type() == RodType.TypeRod.FUEL)) {
                heat += rod.baseRodHeat();
                currentRod = "fuel";
            } else if (currentStack.is(CNTags.CNItemTags.COOLER.tag) || (rod.items().size() > 0 && rod.type() == RodType.TypeRod.COOLER)) {
                heat += rod.baseRodHeat();
                currentRod = "cooler";
            }

            // find position in formattedPattern and check neighbors
            for (int j = 0; j < formattedPattern.length; j++) {
                for (int k = 0; k < formattedPattern[j].length; k++) {
                    if (formattedPattern[j][k] == 99) continue;
                    if (list.getCompound(i).getInt("Slot") != formattedPattern[j][k]) continue;

                    for (int[] offset : offsets) {
                        int nj = j + offset[0];
                        int nk = k + offset[1];
                        if (nj < 0 || nj >= formattedPattern.length || nk < 0 || nk >= formattedPattern[j].length) continue;

                        int neighborSlot = formattedPattern[nj][nk];
                        for (int l = 0; l < list.size(); l++) {
                            if (list.getCompound(l).getInt("Slot") == neighborSlot) {
                                if ("fuel".equals(currentRod)) {
                                    ItemStack stack = ItemStack.of(list.getCompound(l));
                                    if (stack.is(CNTags.CNItemTags.FUEL.tag)) {
                                        heat += rod.proximityRodHeat();
                                    } else if (stack.is(CNTags.CNItemTags.COOLER.tag)) {
                                        heat += rod.baseRodHeat() / ItemRodTypesValue.getRodType(stack.getItem()).proximityRodHeat();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return heat + overHeat;
    }
}
