package net.nuclearteam.createnuclear.content.multiblock.input;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import lib.multiblock.SimpleMultiBlockAislePatternBuilder;
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
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;

import javax.annotation.Nullable;
import java.util.List;

import static net.nuclearteam.createnuclear.content.multiblock.CNMultiblock.*;

public class ReactorInputEntity extends SmartBlockEntity implements MenuProvider {
    protected BlockPos block;
    //protected ReactorControllerBlockEntity controller;
    public ReactorControllerBlock controller = null;

    public ReactorInputInventory inventory;
    LazyOptional<IItemHandler> inventoryProvider;


    public ReactorInputEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory = new ReactorInputInventory(this);
        inventoryProvider = LazyOptional.of(() -> inventory);

    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) { }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        if (!clientPacket) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
        super.read(tag, clientPacket);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        if (!clientPacket) {
            tag.put("Inventory", inventory.serializeNBT());
        }
        super.write(tag, clientPacket);
    }

    public void readInventory(CompoundTag compound) {
        inventory.deserializeNBT(compound);
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
        return ReactorInputMenu.create(i, inventory, this);
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
        if (isItemHandlerCap(cap))
            return inventoryProvider.cast();
        return super.getCapability(cap, side);
    }

    public void setController(ReactorControllerBlock controller) {
        this.controller = controller;
    }
}