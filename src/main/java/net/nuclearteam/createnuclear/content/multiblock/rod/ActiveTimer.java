package net.nuclearteam.createnuclear.content.multiblock.rod;

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
        
        if (remainingTicks <= 0) {
            return true;
        }
        return false;
    }

    public net.minecraft.nbt.CompoundTag serializeNBT() {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("name", name);
        tag.putInt("remainingTicks", remainingTicks);
        tag.putInt("maxTicks", maxTicks);
        tag.putInt("remainingExtracts", remainingExtracts);
        return tag;
    }

    public static ActiveTimer deserializeNBT(net.minecraft.nbt.CompoundTag tag) {
        ActiveTimer timer = new ActiveTimer(tag.getString("name"), tag.getInt("maxTicks"), tag.getInt("remainingExtracts"));
        timer.remainingTicks = tag.getInt("remainingTicks");
        return timer;
    }
}