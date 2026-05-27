package net.nuclearteam.createnuclear.content.multiblock.input.item;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import net.nuclearteam.createnuclear.CNMenus;

public class ReactorInputMenu extends MenuBase<ReactorInputEntity> {


    public ReactorInputMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public ReactorInputMenu(MenuType<?> type, int id, Inventory inv, ReactorInputEntity contentHolder) {
        super(type, id, inv, contentHolder);
    }

    public static ReactorInputMenu create(int id, Inventory inv, ReactorInputEntity contentHolder) {
        return new ReactorInputMenu(CNMenus.SLOT_ITEM_STORAGE.get(), id, inv, contentHolder);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            int playerInventorySize = player.getInventory().items.size(); // normalement 36
            int containerStart = playerInventorySize;
            int containerEnd = containerStart + 1; // 1 slot de machine

            // Si on a cliqué un slot de machine (après les slots joueurs)
            if (index >= containerStart && index < containerEnd) {
                if (!this.moveItemStackTo(stackInSlot, 0, playerInventorySize, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // On a cliqué un slot joueur — essayer d'aller dans le container (machine)
                if (!this.moveItemStackTo(stackInSlot, containerStart, containerEnd, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stackInSlot);
        }
        return result;
    }


    @Override
    protected ReactorInputEntity createOnClient(FriendlyByteBuf extraData) {
        ClientLevel world = Minecraft.getInstance().level;
        BlockEntity entity = world.getBlockEntity(extraData.readBlockPos());
        if (entity instanceof ReactorInputEntity reactorInputEntity) {
            reactorInputEntity.readClient(extraData.readNbt());
            return reactorInputEntity;
        }
        return null;
    }

    @Override
    protected void initAndReadInventory(ReactorInputEntity contentHolder) {

    }

    @Override
    protected void addSlots() {

        // player Slots
        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
            this.addSlot(new Slot(player.getInventory(), hotbarSlot, -31 + hotbarSlot * 18, 155));
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(player.getInventory(), col + row * 9 + 9, -31 + col * 18, 97 + row * 18));
            }
        }

        Slot slot1 = new SlotItemHandler(contentHolder.inventory, 0, 42, 29);

        addSlot(slot1);
    }

    @Override
    protected void saveData(ReactorInputEntity contentHolder) {
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.THROW) {
            int[] targetSlotIds = {9, 18, 27, 0, 1, 28, 19, 10, 16, 17, 26, 25, 34, 35, 8, 7};
            for (int id : targetSlotIds) {
                if (slotId == id) {
                    clickType = ClickType.PICKUP;
                    super.clicked(slotId, button, clickType, player);
                }
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }
}