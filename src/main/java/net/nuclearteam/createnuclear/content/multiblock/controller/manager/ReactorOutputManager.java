package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Manager for a reactor's outputs (`ReactorOutput`).
 * Handles serialization of output positions and distribution
 * of energy via `distributeSU`.
 */
public class ReactorOutputManager extends AbstractReactorIOManager implements ReactorOutputManagerI {
    private static final String NBT_KEY = "ReactorOutputs";

    @Override
    public void write(CompoundTag compound) {
        ListTag list = new ListTag();
        for (BlockPos p : positions) {
            CompoundTag t = new CompoundTag();
            t.putLong("p", p.asLong());
            list.add(t);
        }
        compound.put(NBT_KEY, list);
    }

    @Override
    public void read(CompoundTag compound) {
        positions.clear();
        if (!compound.contains(NBT_KEY)) return;
        ListTag list = compound.getList(NBT_KEY, 10);
        for (int i = 0; i < list.size(); i++) {
            BlockPos p = BlockPos.of(list.getCompound(i).getLong("p"));
            positions.add(p);
        }
    }

    /**
     * Example usage:
     * double totalSUToDistribute = this.reactorPower; // or another value depending on logic
     * if (!reactorOutputEntityList.isEmpty() && totalSUToDistribute > 0) {
     *     double remaining = outputManager.distributeSU(totalSUToDistribute, reactorOutputEntityList, (outEntity, amount) -> {
     *         // Inserter: apply `amount` to the output and return the amount not accepted.
     *         // Here we accept everything and apply the corresponding speed (rounded)
     *         int speed = (int) Math.round(amount);
     *         try {
     *             outEntity.setSpeed(speed);
     *             outEntity.heat = speed; // adjust heat if needed
     *             outEntity.updateSpeed = true;
     *             outEntity.updateGeneratedRotation();
     *             return 0.0; // all accepted
     *         } catch (Exception e) {
     *             return amount; // nothing accepted on error
     *         }
     *     });
     *     // `remaining` contains the SU that couldn't be inserted — you can store or drop it
     *     this.reactorPower = (float) remaining; // example: keep the remainder
     * }
     * @param totalSU total SU to distribute
     * @param level world level (may be null)
     * @param inserter function that applies an amount to an output and returns not accepted amount
     * @return amount of SU not inserted
     */
    public double distributeSU(double totalSU, Level level, BiFunction<ReactorOutputEntity, Double, Double> inserter) {
        if (totalSU <= 0 || inserter == null) return totalSU;
        List<ReactorOutputEntity> validOutputs = new ArrayList<>();
        for (BlockPos p : new ArrayList<>(positions)) {
            if (level == null || !level.isLoaded(p)) continue;
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof ReactorOutputEntity out) validOutputs.add(out);
        }
        if (validOutputs.isEmpty()) return totalSU;

        double remaining = totalSU;
        List<ReactorOutputEntity> active = new ArrayList<>(validOutputs);
        final double MIN_ACCEPTED_SU = 1e-6;

        // Distribute evenly and reallocate the remainder among outputs still able to accept
        boolean progress = true;
        while (remaining > MIN_ACCEPTED_SU && !active.isEmpty() && progress) {
            progress = false;
            double share = remaining / active.size();
            List<ReactorOutputEntity> stillActive = new ArrayList<>();

            for (ReactorOutputEntity out : active) {
                double toInsert = share;
                double notAccepted;
                try {
                    notAccepted = inserter.apply(out, toInsert);
                } catch (Exception e) {
                    notAccepted = toInsert;
                }
                if (Double.isNaN(notAccepted) || notAccepted < 0) notAccepted = 0;
                if (notAccepted > toInsert) notAccepted = toInsert;
                double inserted = toInsert - notAccepted;

                if (inserted > MIN_ACCEPTED_SU) {
                    progress = true;
                    remaining -= inserted;
                    // keep this output active; it may accept more in the next round
                    stillActive.add(out);
                }
                // if inserted == 0, remove it (do not attempt to send more)
            }

            active = stillActive;
        }

        return remaining;
    }

    @Override
    public void clearInvalid(Level level) {
        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos p : positions) {
            if (level == null || !level.isLoaded(p)) {
                toRemove.add(p);
                continue;
            }
            BlockEntity be = level.getBlockEntity(p);
            if (!(be instanceof ReactorOutputEntity)) toRemove.add(p);
        }
        positions.removeAll(toRemove);
    }
}
