package net.nuclearteam.createnuclear.content.multiblock.rod;

import net.minecraft.nbt.CompoundTag;

public class ActiveTimer {
    public String name;
    public int remainingTicks;
    public int maxTicks;
    public int remainingExtracts;

    public ActiveTimer(String name, int ticks, int extracts) {
        this.name = name;
        this.remainingTicks = ticks;
        this.maxTicks = ticks;
        this.remainingExtracts = extracts;
    }

    public ActiveTimer(String name, int ticks) {
        this(name, ticks, 1);
    }

    public boolean tick() {
        if (remainingTicks > 0) remainingTicks--;

        return remainingTicks <= 0;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putInt("remainingTicks", remainingTicks);
        tag.putInt("maxTicks", maxTicks);
        tag.putInt("remainingExtracts", remainingExtracts);
        return tag;
    }

    public static ActiveTimer deserializeNBT(CompoundTag tag) {
        ActiveTimer timer = new ActiveTimer(tag.getString("name"), tag.getInt("maxTicks"), tag.getInt("remainingExtracts"));
        timer.remainingTicks = tag.getInt("remainingTicks");
        return timer;
    }
}