package net.nuclearteam.createnuclear.content.multiblock.controller.snapshot;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.ReactorInputFluidManagerI;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.ReactorInputManagerI;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.VirtualReactorInputFluid;
import net.nuclearteam.createnuclear.content.multiblock.input.item.VirtualReactorInputsItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReactorInputSnapshotBuilder {
    private ReactorInputSnapshotBuilder() {}

    public static ReactorInputSnapshot build(Level level, ReactorInputManagerI inputManager, ReactorInputFluidManagerI inputFluidManager) {
        // Populate display fields for client sync
        Map<Item, Integer> items = new HashMap<>();
        List<IItemHandler> itemHandlers = inputManager.getItemHandlers(level);
        for (IItemHandler h : itemHandlers) {
            for (int s = 0; s < h.getSlots(); s++) {
                ItemStack st = h.getStackInSlot(s);
                if (!st.isEmpty()) {
                    items.merge(st.getItem(), st.getCount(), Integer::sum);
                }
            }
        }

        long maxFluidCapacity = 0;
        for (IFluidHandler h : inputFluidManager.getFuildHandlers(level)) {
            if (h.getTanks() > 0) {
                maxFluidCapacity += h.getTankCapacity(0);
            }
        }

        VirtualReactorInputsItem virtualItems = inputManager.getInventory(level);
        VirtualReactorInputFluid virtualFluid = inputFluidManager.getInventory(level);
        List<BigFluidStack> fluids = VirtualReactorInputFluid.toBigList(virtualFluid.fluids());



        return new ReactorInputSnapshot(items, fluids, maxFluidCapacity);
    }
}
