package net.nuclearteam.createnuclear.content.multiblock.controller.service;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;

public class DefaultPersistenceService implements IPersistenceService {
    @Override
    public void readBasicState(ReactorControllerBlockEntity owner, CompoundTag compound, boolean clientPacket) {
        owner.setMultiblockSize(compound.getInt("reactorSize"));
        owner.setMultiblockFacing(compound.getString("reactorFacing"));
        owner.setMultiblockStructure(compound.getIntArray("reactorPose"));
        owner.setLiquidLife(compound.getDouble("liquidLife"));

        if (!clientPacket) {
            owner.deserializeInventory(compound.getCompound("pattern"));
        }
        owner.setConfiguredPattern(ItemStack.of(compound.getCompound("items")));

        owner.setBigFuelItem(BigItemStack.read(compound.getCompound("bigFuel")));
        owner.setBigCoolerItem(BigItemStack.read(compound.getCompound("bigCooler")));
    }

    @Override
    public void writeBasicState(ReactorControllerBlockEntity owner, CompoundTag compound, boolean clientPacket) {
        compound.putInt("reactorSize", owner.getMultiblockSize());
        compound.putString("reactorFacing", owner.getMultiblockFacing());
        if (owner.getMultiblockPos() != null) {
            compound.putIntArray("reactorPose", owner.getMultiblockPos());
        }

        if (!clientPacket) {
            compound.put("pattern", owner.serializeInventory());
        }
        compound.put("items", owner.getConfiguredPattern().serializeNBT());

        compound.put("bigFuel", owner.getBigFuelItem().write());
        compound.put("bigCooler", owner.getBigCoolerItem().write());
    }
}
