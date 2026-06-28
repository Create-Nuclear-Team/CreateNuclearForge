package net.nuclearteam.createnuclear.content.multiblock.input.item;


import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType.TypeRodPredicate;
import org.jetbrains.annotations.NotNull;

public class ReactorRodInputInventory extends ItemStackHandler {
    private final ReactorRodInputEntity be;

    public ReactorRodInputInventory(ReactorRodInputEntity be) {
        super(1);
        this.be = be;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        be.setChanged();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        Level level = be.getLevel();
        return switch (slot) {
            case 0 -> level != null && (TypeRodPredicate.isFuel(stack, level) || TypeRodPredicate.isCooled(stack, level));
            default -> !super.isItemValid(slot, stack);
        };
    }
}