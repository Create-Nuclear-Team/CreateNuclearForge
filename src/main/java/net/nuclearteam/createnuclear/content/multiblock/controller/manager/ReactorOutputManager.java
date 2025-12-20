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
import java.util.function.Function;

public class ReactorOutputManager extends AbstractReactorIOManager {
    private final List<BlockPos> outputPositions = new ArrayList<>();
    private static final String NBT_KEY = "ReactorOutputs";

    @Override
    public boolean addBlock(BlockPos pos) {
        if (pos == null) return false;
        if (!this.contains(pos)) {
            outputPositions.add(pos);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeBlock(BlockPos pos) {
        return outputPositions.remove(pos);
    }

    @Override
    public boolean contains(BlockPos pos) {
        return outputPositions.contains(pos);
    }

    @Override
    public int size() {
        return outputPositions.size();
    }

    @Override
    public List<BlockPos> getBlocksPosition() {
        return List.copyOf(outputPositions);
    }

    @Override
    public void write(CompoundTag compound) {
        ListTag list = new ListTag();
        for (BlockPos p : outputPositions) {
            CompoundTag t = new CompoundTag();
            t.putLong("p", p.asLong());
            list.add(t);
        }
        compound.put(NBT_KEY, list);
    }

    @Override
    public void read(CompoundTag compound) {
        outputPositions.clear();
        if (!compound.contains(NBT_KEY)) return;
        ListTag list = compound.getList(NBT_KEY, 10);
        for (int i = 0; i < list.size(); i++) {
            BlockPos p = BlockPos.of(list.getCompound(i).getLong("p"));
            outputPositions.add(p);
        }
    }

    /**
     * double totalSUToDistribute = this.reactorPower; // ou total / autre valeur selon ta logique
     *         if (!reactorOutputEntityList.isEmpty() && totalSUToDistribute > 0) {
     *             double remaining = outputManager.distributeSU(totalSUToDistribute, reactorOutputEntityList, (outEntity, amount) -> {
     *                 // Inserter : applique amount à l'output et retourne la quantité non acceptée.
     *                 // Ici on accepte tout et on applique la vitesse correspondante (arrondie)
     *                 int speed = (int) Math.round(amount);
     *                 try {
     *                     outEntity.setSpeed(speed);           // tu utilises déjà setSpeed(...)
     *                     outEntity.heat = speed;              // ajustement heat si nécessaire
     *                     outEntity.updateSpeed = true;
     *                     outEntity.updateGeneratedRotation();
     *                     return 0.0; // tout accepté
     *                 } catch (Exception e) {
     *                     return amount; // rien accepté en cas d'erreur
     *                 }
     *             });
     *             // remaining contient la SU non insérée — tu peux la stocker ou la perdre selon ta logique
     *             this.reactorPower = (float) remaining; // exemple : garder le reste
     *         }
     * @param totalSU
     * @param level
     * @param inserter
     * @return
     */
    public double distributeSU(double totalSU, Level level, BiFunction<ReactorOutputEntity, Double, Double> inserter) {
        if (totalSU <= 0 || inserter == null) return totalSU;
        List<ReactorOutputEntity> validOutputs = new ArrayList<>();
        for (BlockPos p : new ArrayList<>(outputPositions)) {
            if (level == null || !level.isLoaded(p)) continue;
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof ReactorOutputEntity out) validOutputs.add(out);
        }
        int n = validOutputs.size();
        if (n == 0) return totalSU;

        double share = totalSU / n;
        double remaining = totalSU;

        for (int i = 0; i < n && remaining > 0.0; i++) {
            ReactorOutputEntity out = validOutputs.get(i);
            double toInsert = Math.min(share, remaining);
            double notAccepted;
            try {
                notAccepted = inserter.apply(out, toInsert);
            } catch (Exception e) {
                notAccepted = toInsert;
            }
            if (Double.isNaN(notAccepted) || notAccepted < 0) notAccepted = 0;
            if (notAccepted > toInsert) notAccepted = toInsert;
            double inserted = toInsert - notAccepted;
            remaining -= inserted;
        }
        return remaining;
    }

    @Override
    public void clearInvalid(Level level) {
        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos p : outputPositions) {
            if (level == null || !level.isLoaded(p)) {
                toRemove.add(p);
                continue;
            }
            BlockEntity be = level.getBlockEntity(p);
            if (!(be instanceof ReactorOutputEntity)) toRemove.add(p);
        }
        outputPositions.removeAll(toRemove);
    }

    @Override
    public <T> List<T> resolveBlock(Level level, Function<BlockPos, T> resolver) {
        List<T> result = new ArrayList<>();
        for (BlockPos p: new ArrayList<>(outputPositions)) {
            T r = resolver.apply(p);
            if (r != null) result.add(r);
        }
        return result;
    }
}
