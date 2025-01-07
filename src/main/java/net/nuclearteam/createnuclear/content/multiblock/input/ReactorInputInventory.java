package net.nuclearteam.createnuclear.content.multiblock.input;


import net.minecraftforge.items.ItemStackHandler;

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
    public boolean isItemValid(int slot, ItemVariant resource, int count) {
        return switch (slot) {
            case 0 -> CNItems.URANIUM_ROD.get() == resource.getItem();
            case 1 -> CNItems.GRAPHITE_ROD.get() == resource.getItem();
            default -> !super.isItemValid(slot, resource, count);
        };
    }
}