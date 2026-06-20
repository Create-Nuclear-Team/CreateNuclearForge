package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class ReactorFrameDisplayManager implements ReactorFrameDisplayManagerI {
    public static final String COMPONENT_FRAME_COLUMN_MIN_Y = "frameColumnMinY";
    public static final String COMPONENT_FRAME_COLUMN_MAX_Y = "frameColumnMaxY";

    // Vertical span (block Y) of the reactor frame columns, used to map the
    // fluid fill ratio onto the liquid actually drawn in the frame windows.
    private int frameColumnMinY = Integer.MAX_VALUE;
    private int frameColumnMaxY = Integer.MIN_VALUE;

    // Per-tick cache for the fluid drawn in the frame windows. Recomputed from the
    // live (synced) input tanks so the displayed fluid and its fill ratio always
    // come from the same source and stay consistent for every fluid type.
    private long frameFluidCacheTick = -1;
    private float frameFluidFillRatioCache = 0f;


    private FluidStack frameFluidCache = FluidStack.EMPTY;

    /**
     * Returns the fluid currently held by the reactor, used by the frame
     * renderer to draw the matching liquid in the window. On the client this
     * reads the synced ; on the server it reads the
     * aggregated . Returns {@link FluidStack#EMPTY} when
     * the reactor holds no fluid.
     * <p>
     * Recomputes, at most once per game tick, the fluid shown in the frame
     * windows and how full the input is. Both values are read from the same live
     * input tanks (whose contents are synced to the client), so they always agree
     * regardless of the fluid type.
     */
    private void refreshFrameFluidCache(@Nullable Level level, ReactorInputFluidManagerI handlers) {
        if (level == null) return;
        long now = level.getGameTime();

        if (now == frameFluidCacheTick) return;
        frameFluidCacheTick = now;

        long amount = 0, capacity = 0;
        FluidStack fluid = FluidStack.EMPTY;

        for (IFluidHandler handler : handlers.getFuildHandlers(level)) {
            int tanks = handler.getTanks();
            for (int t = 0; t < tanks; t++) {
                FluidStack stack = handler.getFluidInTank(t);
                capacity += handler.getTankCapacity(t);
                amount += stack.getAmount();

                if (fluid.isEmpty() && !stack.isEmpty()) fluid = stack.copy();
            }
        }

        frameFluidCache = fluid;
        frameFluidFillRatioCache = capacity > 0 ? Math.min(1f, (float) amount / (float) capacity) : 0f;
    }

    /**
     * The fluid currently shown in the reactor frame windows (may be empty).
     */
    @Override
    public FluidStack getDisplayedFluid(Level level, ReactorInputFluidManagerI handlers) {
        refreshFrameFluidCache(level, handlers);

        return frameFluidCache;
    }

    /**
     * How full the reactor's fluid input is, in the range {@code [0, 1]}, used by
     * the frame renderer to size the visible liquid column.
     */
    @Override
    public float getDisplayedFluidFillRatio(Level level, ReactorInputFluidManagerI handlers) {
        refreshFrameFluidCache(level, handlers);

        return frameFluidFillRatioCache;
    }

    /**
     * Records the lowest and highest frame block-Y of the assembled reactor.
     */
    @Override
    public void setFrameColumn(int minY, int maxY, Runnable onChange) {
        if (this.frameColumnMinY == minY && this.frameColumnMaxY == maxY) return;
        this.frameColumnMinY = minY;
        this.frameColumnMaxY = maxY;
        onChange.run();
    }

    /**
     * @return the lowest frame block-Y, or {@link Integer#MAX_VALUE} if unknown.
     */
    @Override
    public int getFrameColumnMinY() {
        return frameColumnMinY;
    }

    /**
     * @return the highest frame block-Y, or {@link Integer#MIN_VALUE} if unknown.
     */
    @Override
    public int getFrameColumnMaxY() {
        return frameColumnMaxY;
    }

    @Override
    public boolean hasFrameColumn() {
        return frameColumnMinY != Integer.MAX_VALUE && frameColumnMaxY != Integer.MIN_VALUE
                && frameColumnMaxY >= frameColumnMinY;
    }

    @Override
    public void read(CompoundTag compound) {
        if (compound.contains(COMPONENT_FRAME_COLUMN_MIN_Y)) {
            frameColumnMinY = compound.getInt(COMPONENT_FRAME_COLUMN_MIN_Y);
        }

        if (compound.contains(COMPONENT_FRAME_COLUMN_MAX_Y)) {
            frameColumnMaxY = compound.getInt(COMPONENT_FRAME_COLUMN_MAX_Y);
        }
    }

    @Override
    public void write(CompoundTag compound) {
        compound.putInt(COMPONENT_FRAME_COLUMN_MIN_Y, frameColumnMinY);
        compound.putInt(COMPONENT_FRAME_COLUMN_MAX_Y, frameColumnMaxY);
    }
}
