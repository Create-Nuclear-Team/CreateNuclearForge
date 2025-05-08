package net.nuclearteam.createnuclear.content.multiblock.controller;

import com.simibubi.create.foundation.item.SmartInventory;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.CNItems;

public class ReactorControllerInventory extends SmartInventory {
    private final ReactorControllerBlockEntity be;

    public ReactorControllerInventory(ReactorControllerBlockEntity be) {
        super(1, be, 1, false);
        this.be = be;
    }


    @Override
    public ItemStack removeItemNoUpdate(int index) {
        be.setChanged();
        return super.removeItemNoUpdate(index);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack resource) {
        return switch (slot) {
            case 0 -> CNItems.REACTOR_BLUEPRINT.get() == resource.getItem();
            default -> !super.isItemValid(slot, resource);
        };
    }
}