package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

/**
 * Manages the display state of the reactor frame windows: the fluid shown,
 * its fill ratio, and the vertical column bounds of the assembled frame.
 */
public interface ReactorFrameDisplayManagerI {
    /**
     * Returns the fluid currently shown in the reactor frame windows (may be
     * empty). Recomputes the underlying cache at most once per game tick from
     * the given fluid handlers.
     */
    FluidStack getDisplayedFluid(Level level, ReactorInputFluidManagerI handlers);

    /**
     * Returns how full the reactor's fluid input is, in the range {@code [0, 1]}.
     * Recomputes the underlying cache at most once per game tick from the
     * given fluid handlers.
     */
    float getDisplayedFluidFillRatio(Level level, ReactorInputFluidManagerI handlers);

    /**
     * Records the lowest and highest frame block-Y of the assembled reactor.
     * If the values changed, {@code onChange} is invoked (e.g. to trigger a
     * client sync via {@code notifyUpdate()}).
     */
    void setFrameColumn(int minY, int maxY, Runnable onChange);

    /** @return the lowest frame block-Y, or {@link Integer#MAX_VALUE} if unknown. */
    int getFrameColumnMinY();

    /** @return the highest frame block-Y, or {@link Integer#MIN_VALUE} if unknown. */
    int getFrameColumnMaxY();

    /** @return true if both frame column bounds are known and consistent. */
    boolean hasFrameColumn();

    /** Reads state from an NBT tag (deserialization). */
    void read(CompoundTag compound);

    /** Writes state to an NBT tag (serialization). */
    void write(CompoundTag compound);
}
