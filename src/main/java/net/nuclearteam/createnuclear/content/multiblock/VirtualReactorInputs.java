package net.nuclearteam.createnuclear.content.multiblock;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CNTags.CNItemTags;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.ReactorInputManagerI;
import org.jetbrains.annotations.NotNull;

public class VirtualReactorInputs extends ItemStackHandler {
    public VirtualReactorInputs(ReactorInputManagerI input) {
        super(2 * (input.size() <= 0 ? 1 : input.size()));
    }

    public void clear() {
        for (int i = 0; i < getSlots(); i++)
            setStackInSlot(i, ItemStack.EMPTY);
    }

    public boolean isEmpty() {
        for (int i = 0; i < getSlots(); i++)
            if (!getStackInSlot(i).isEmpty())
                return false;
        return true;
    }

    public @NotNull ItemStack insertFuelItem(ItemStack stack, boolean simulate) {
        return insertItem(0, stack, simulate);
//        if (stack.is(CNItemTags.FUEL.tag))
//        return ItemStack.EMPTY;
    }

    public @NotNull ItemStack insertCoolerItem(ItemStack stack, boolean simulate) {
        return insertItem(1, stack, simulate);
//        if (stack.is(CNItemTags.COOLER.tag))
//        return ItemStack.EMPTY;
    }

    public ItemStack getFuelSlot() {
        return getStackInSlot(0);
    }

    public ItemStack getCoolerSlot() {
        return getStackInSlot(1);
    }

    public record VirtualReactorInputsR(int fuel, int cooler) {
        public VirtualReactorInputsR() {
            this(0,0);
        }

        public ItemStack getFuelRod() {
            return new ItemStack(CNItems.URANIUM_ROD.asItem(), fuel);
        }

        public ItemStack getCooledRod() {
            return new ItemStack(CNItems.GRAPHITE_ROD.asItem(), cooler);
        }

        @Override
        public @NotNull String toString() {
            return "fuel: " + fuel + " cooler: " + cooler;
        }

        public void write(CompoundTag tag) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("fuel", fuel);
            nbt.putInt("cooler", cooler);
            tag.put("reactorInventory", nbt);
        }

        public VirtualReactorInputsR read(CompoundTag tag) {
            CompoundTag nbt = tag.getCompound("reactorInventory");

            return new VirtualReactorInputsR(nbt.getInt("fuel"), nbt.getInt("cooler"));
        }
    }
}
