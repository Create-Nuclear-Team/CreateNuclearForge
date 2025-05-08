package net.nuclearteam.createnuclear.content.multiblock.input;


import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.nuclearteam.createnuclear.CNItems;
import org.jetbrains.annotations.NotNull;

public class ReactorInputInventory extends ItemStackHandler {
    private final ReactorInputEntity be;

    public ReactorInputInventory(ReactorInputEntity be) {
        super(2);
        this.be = be;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        be.setChanged();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return switch (slot) {
            case 0 -> CNItems.URANIUM_ROD.get() == stack.getItem();
            case 1 -> CNItems.GRAPHITE_ROD.get() == stack.getItem();
            default -> !super.isItemValid(slot, stack);
        };
    }
}