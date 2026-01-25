package net.nuclearteam.createnuclear.content.multiblock;

import com.simibubi.create.content.logistics.BigItemStack;
import net.nuclearteam.createnuclear.CNItems;
import org.jetbrains.annotations.NotNull;

public record VirtualReactorInputs(int fuel, int cooler) {
    public VirtualReactorInputs() {
        this(0,0);
    }

    public BigItemStack getBigFuelRod() {
        return new BigItemStack(CNItems.URANIUM_ROD.asStack(), fuel);
    }

    public BigItemStack getBigCooledRod() {
        return new BigItemStack(CNItems.GRAPHITE_ROD.asStack(), cooler);
    }

    @Override
    public @NotNull String toString() {
        return "fuel: " + fuel + " cooler: " + cooler;
    }
}

