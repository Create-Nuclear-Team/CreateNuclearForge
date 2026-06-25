package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType.TypeRodPredicate;
import net.nuclearteam.createnuclear.content.multiblock.input.item.VirtualReactorInputsItem;
import net.nuclearteam.createnuclear.content.multiblock.input.item.ReactorRodInputEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for reactor input positions (`ReactorInput`).
 *
 * Serializes positions as x/y/z triplets and provides utilities to
 * obtain valid `IItemHandler` instances present at those positions.
 */
public class ReactorInputManager extends AbstractReactorIOManager implements ReactorInputManagerI {
    private static final String NBT_KEY = "ReactorInput";

    @Override
    public void write(CompoundTag compound) {
        ListTag list = new ListTag();
        for (BlockPos pos : positions) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            list.add(tag);
        }
        compound.put(NBT_KEY, list);
    }

    @Override
    public void read(CompoundTag compound) {
        positions.clear();
        if (!compound.contains(NBT_KEY)) return;
        ListTag list = compound.getList(NBT_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag tag = list.getCompound(i);
            positions.add(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
        }
    }

    /**
     * Retrieves all `IItemHandler` instances located at the input positions.
     * Returns an empty list when no handlers are found.
     */
    @Override
    public List<IItemHandler> getItemHandlers(Level level) {
        List<IItemHandler> handlers = new ArrayList<>();
        for (BlockPos p: new ArrayList<>(positions)) {
            if (level == null || !level.isLoaded(p)) continue;
            BlockEntity be = level.getBlockEntity(p);
            if (be == null) continue;
            LazyOptional<IItemHandler> cap = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
            cap.ifPresent(handlers::add);
        }

        return handlers;
    }

    @Override
    public VirtualReactorInputsItem getInventory(Level level) {
        List<IItemHandler> handlers = this.getItemHandlers(level);
        if (handlers.isEmpty()) return new VirtualReactorInputsItem();

        int totalFuel = 0;
        int totalCooler = 0;
        for (IItemHandler h : handlers) {
            int slots = h.getSlots();
            for (int s = 0; s < slots; s++) {
                ItemStack st = h.getStackInSlot(s);

                if (TypeRodPredicate.IS_FUEL.test(st)) totalFuel += st.getCount();
                else if (TypeRodPredicate.IS_COOLED.test(st)) totalCooler += st.getCount();
            }
        }

        return new VirtualReactorInputsItem(totalFuel, totalCooler);
    }

    @Override
    public boolean extractItems(Level level, int fuelNeeded, int coolerNeeded) {
        if (level == null) return false;
        List<IItemHandler> handlers = getItemHandlers(level);
        if (handlers.isEmpty()) return false;

        int fuelRemaining = fuelNeeded;
        int coolerRemaining = coolerNeeded;

        for (IItemHandler handler : handlers) {
            int slots = handler.getSlots();
            for (int s = 0; s < slots && (fuelRemaining > 0 || coolerRemaining > 0); s++) {
                ItemStack stack = handler.getStackInSlot(s);
                if (stack.isEmpty()) continue;


                if (fuelRemaining > 0 && TypeRodPredicate.IS_FUEL.test(stack)) {
                    int toExtract = Math.min(fuelRemaining, stack.getCount());
                    handler.extractItem(s, toExtract, false);
                    fuelRemaining -= toExtract;
                } else if (coolerRemaining > 0 && TypeRodPredicate.IS_COOLED.test(stack)) {
                    int toExtract = Math.min(coolerRemaining, stack.getCount());
                    handler.extractItem(s, toExtract, false);
                    coolerRemaining -= toExtract;
                }
            }
            if (fuelRemaining <= 0 && coolerRemaining <= 0) break;
        }

        return fuelRemaining <= 0 && coolerRemaining <= 0;
    }

    public boolean extractItemByName(Level level, String itemName) {
        if (level == null || itemName == null) return false;

        List<IItemHandler> handlers = getItemHandlers(level);
        if (handlers.isEmpty()) return false;

        for (IItemHandler handler : handlers) {
            int slots = handler.getSlots();
            for (int s = 0; s < slots; s++) {
                ItemStack stack = handler.getStackInSlot(s);
                if (stack.isEmpty()) continue;

                // Resolve the item's registry name (e.g. "uranium_rod")
                String registryPath = ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath();

                // Lenient comparison: ignores case and underscores (_)
                if (isMatching(registryPath, itemName)) {
                    // Attempt to extract 1 unit
                    ItemStack extracted = handler.extractItem(s, 1, false);

                    // If extraction succeeded, stop here and return true
                    if (!extracted.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false; // The requested item was not found
    }

    /**
     * Helper to compare names like "GraphiteRod" against "graphite_rod"
     * (case-insensitive, underscore-insensitive).
     */
    private boolean isMatching(String registryPath, String configName) {
        String cleanPath = registryPath.replace("_", "").toLowerCase();
        String cleanConfig = configName.replace("_", "").toLowerCase();
        return cleanPath.equals(cleanConfig);
    }

    /**
     * Cleans up invalid positions (unloaded chunk, missing block entity,
     * or not a `Container`).
     */
    @Override
    public void clearInvalid(Level level) {
        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos p: positions) {
            if (level == null || !level.isLoaded(p)) {
                toRemove.add(p);
                continue;
            }

            BlockEntity be = level.getBlockEntity(p);
            if (be == null || !(be instanceof Container)) toRemove.add(p);
        }

        positions.removeAll(toRemove);
    }

    @Override
    public List<BlockPos> getBlocksPosition(Level level) {
        List<BlockPos> positions = new ArrayList<>();

        for (BlockPos p : this.getBlocksPosition()) {
            if (level.getBlockEntity(p) instanceof ReactorRodInputEntity) positions.add(p);
        }
        return List.copyOf(positions);
    }
}
