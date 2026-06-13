package net.nuclearteam.createnuclear.content.multiblock.controller.service;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;

public class DefaultPersistenceService implements IPersistenceService {
    @Override
    public void readBasicState(ReactorControllerBlockEntity owner, CompoundTag compound, boolean clientPacket) {
        owner.setMultiblockSize(compound.getInt("reactorSize"));
        owner.setMultiblockFacing(Direction.byName(compound.getString("reactorFacing")));

        owner.setMultiblockStructure(compound.contains("reactorPose")
            ? BoundingBox.CODEC.parse(NbtOps.INSTANCE, compound.get("reactorPose")).result().orElse(null)
            : null
        );
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
        compound.putString("reactorFacing", owner.getMultiblockFacing() != null ? owner.getMultiblockFacing().getSerializedName() : "");
        if (owner.getMultiblockPos() != null) {
            compound.put("reactorPose", BoundingBox.CODEC.encodeStart(NbtOps.INSTANCE, owner.getMultiblockPos()).getOrThrow(false, IllegalStateException::new));
        }

        if (!clientPacket) {
            compound.put("pattern", owner.serializeInventory());
        }
        compound.put("items", owner.getConfiguredPattern().serializeNBT());

        compound.put("bigFuel", owner.getBigFuelItem().write());
        compound.put("bigCooler", owner.getBigCoolerItem().write());
    }
}
