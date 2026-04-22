package net.nuclearteam.createnuclear.content.multiblock.input.item;


import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.api.ItemRodTypesValue;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType.TypeRod;
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
        RodType rodType = ItemRodTypesValue.getRodType(stack.getItem());
        return switch (slot) {
            case 0 -> CNTags.CNItemTags.FUEL.matches(stack) || rodType.type() == TypeRod.FUEL || rodType.type() == TypeRod.MIXTE;
            case 1 -> CNTags.CNItemTags.COOLER.matches(stack) || rodType.type() == TypeRod.COOLER || rodType.type() == TypeRod.MIXTE;
            default -> !super.isItemValid(slot, stack);
        };
    }
}