package net.nuclearteam.createnuclear.content.multiblock.controller;

import com.simibubi.create.foundation.item.SmartInventory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.CNItems;

@MethodsReturnNonnullByDefault
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
        return slot == 0 && resource.is(CNItems.REACTOR_BLUEPRINT.get());
    }
}