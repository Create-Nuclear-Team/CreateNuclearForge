package net.nuclearteam.createnuclear.content.multiblock.input.item;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.nuclearteam.createnuclear.foundation.block.MultiDirectionalReactorBlock;

import javax.annotation.Nullable;
import java.util.List;

public class ReactorRodInputEntity extends SmartBlockEntity implements MenuProvider {
    //protected ReactorControllerBlockEntity controller;
    public ReactorControllerBlock controller = null;

    public ReactorRodInputInventory inventory;
    LazyOptional<IItemHandler> inventoryProvider;


    public ReactorRodInputEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory = new ReactorRodInputInventory(this);
        inventoryProvider = LazyOptional.of(() -> inventory);

    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) { }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket); // Always call super first

        if (!clientPacket) {
            tag.put("Inventory", inventory.serializeNBT());
        }
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);

        if (!clientPacket) {
            if (tag.contains("Inventory")) {
                inventory.deserializeNBT(tag.getCompound("Inventory"));
            }
        }
    }

    @Nullable
    @Override
    public Level getLevel() {
        return super.getLevel();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.createnuclear.reactor_input.title");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return ReactorRodInputMenu.create(i, inventory, this);
    }

    @Override
    public void tick() {
        super.tick();
    }

    protected boolean isItemHandlerCap(Capability<?> cap) {
        return cap == ForgeCapabilities.ITEM_HANDLER;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (isItemHandlerCap(cap)){
            if (side != null && side != getBlockState().getValue(MultiDirectionalReactorBlock.FACING))
                return LazyOptional.empty();
            return inventoryProvider.cast();
        }

        return super.getCapability(cap, side);
    }

    public void setController(ReactorControllerBlock controller) {
        this.controller = controller;
    }
}