package net.nuclearteam.createnuclear.content.multiblock.input.item;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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

import javax.annotation.Nullable;
import java.util.List;

public class ReactorInputEntity extends SmartBlockEntity implements MenuProvider {
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

    // Dans ta classe, j'imagine que tu as ces variables :
    private BlockPos inputPos;
    private ResourceKey<Level> inputLevelKey; // On stocke la clé, pas l'objet Level directement

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket); // Toujours appeler le super

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