package net.nuclearteam.createnuclear.content.multiblock.controller.consumable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.ReactorInputManagerI;

import java.util.*;

public class ConsumptionCycleManager {

    private final List<ConsumableTimer> timers = new ArrayList<>();
    private Map<String, Integer> patternSnapshot = new HashMap<>();

    public void startCycle(ItemStack pattern, Level level) {
        timers.clear();
        buildTimers(pattern, level);
        takeSnapshot(pattern, level);
    }

    public void resetCycle(ItemStack pattern, Level level, ReactorInputManagerI manager) {
        Map<Item, Integer> counts = PatternReader.readItemCounts(pattern);
        List<ConsumableTimer> updated = new ArrayList<>();

        for (Map.Entry<Item, Integer> entry : counts.entrySet()) {
            RodType rodType = RodType.resolveRodType(entry.getKey(), level);
            if (!rodType.isNotEmptyItem())
                continue;

            String id = ForgeRegistries.ITEMS.getKey(entry.getKey()).getPath();
            int count = entry.getValue();
            int ticks = new ItemConsumable(id, rodType).computeTimer(count);

            timers.stream()
                    .filter(t -> t.getId().equals(id))
                    .findFirst()
                    .ifPresentOrElse(
                            existing -> {
                                existing.reset(ticks, count);
                                updated.add(existing);
                            },
                            () -> updated.add(new ConsumableTimer(
                                    new ItemConsumable(id, rodType), ticks, count)));
        }

        timers.clear();
        timers.addAll(updated);
        takeSnapshot(pattern, level);
    }

    public boolean tick(ReactorInputManagerI manager, Level level) {
        boolean consumed = false;
        Iterator<ConsumableTimer> it = timers.iterator();

        while (it.hasNext()) {
            ConsumableTimer timer = it.next();
            if (timer.tick()) {
                timer.getConsumable().consume(manager, level);
                consumed = true;

                if (timer.getCountInPattern() > 1) {
                    timer.reset(timer.getMaxTicks(), timer.getCountInPattern());
                } else {
                    it.remove();
                }
            }
        }
        return consumed;
    }

    public boolean hasPatternChanged(ItemStack pattern, Level level) {
        Map<String, Integer> current = readItemIds(pattern, level);
        if (current.size() != patternSnapshot.size())
            return true;
        for (Map.Entry<String, Integer> e : current.entrySet())
            if (!e.getValue().equals(patternSnapshot.get(e.getKey())))
                return true;
        return false;
    }

    public boolean isEmpty() {
        return timers.isEmpty();
    }

    public void clear() {
        timers.clear();
        patternSnapshot.clear();
    }

    public CompoundTag serializeNBT() {
        ListTag list = new ListTag();
        timers.forEach(t -> list.add(t.serializeNBT()));
        CompoundTag tag = new CompoundTag();
        tag.put("timers", list);
        CompoundTag snap = new CompoundTag();
        patternSnapshot.forEach(snap::putInt);
        tag.put("snapshot", snap);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        timers.clear();
        tag.getList("timers", Tag.TAG_COMPOUND)
                .forEach(t -> timers.add(ConsumableTimer.deserializeNBT((CompoundTag) t)));
        CompoundTag snap = tag.getCompound("snapshot");
        snap.getAllKeys().forEach(k -> patternSnapshot.put(k, snap.getInt(k)));
    }

    private void buildTimers(ItemStack pattern, Level level) {
        PatternReader.readItemCounts(pattern).forEach((item, count) -> {
            RodType rodType = RodType.resolveRodType(item, level);
            if (!rodType.isNotEmptyItem())
                return;
            String id = ForgeRegistries.ITEMS.getKey(item).getPath();
            int ticks = new ItemConsumable(id, rodType).computeTimer(count);
            timers.add(new ConsumableTimer(new ItemConsumable(id, rodType), ticks, count));
        });
    }

    private void takeSnapshot(ItemStack pattern, Level level) {
        patternSnapshot = readItemIds(pattern, level);
    }

    private Map<String, Integer> readItemIds(ItemStack pattern, Level level) {
        Map<String, Integer> result = new HashMap<>();
        PatternReader.readItemCounts(pattern).forEach((item, count) -> {
            RodType rt = RodType.resolveRodType(item, level);
            if (rt.isNotEmptyItem())
                result.put(ForgeRegistries.ITEMS.getKey(item).getPath(), count);
        });
        return result;
    }
}
