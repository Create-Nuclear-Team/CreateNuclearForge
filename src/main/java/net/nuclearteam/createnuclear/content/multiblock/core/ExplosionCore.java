package net.nuclearteam.createnuclear.content.multiblock.core;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.*;
import java.util.function.Predicate;

public class ExplosionCore implements Comparator<ExplosionCore.NukeTask> {

     public static final int FLAG_IN_EXPLOSION = 1 << 0;
     public static final int FLAG_IS_REINFORCED = 1 << 1;
     public static final int FLAG_DESTROY = 1 << 2;
     public static final int FLAG_IS_AIR = 1 << 3;
     public static final int FLAG_IS_BLOCK = 1 << 4;
     public static final int FLAG_INSIDE = 1 << 5;
     public static final int FLAG_OUTSIDE = 1 << 6;
    private final Level level;
    private final BlockPos pos;
    private final Double radius;

    public static boolean hasFlag(int flags, int flag) {
        return (flags & flag) != 0;
    }

    public static boolean hasFlag(int flags, int flag1, int flag2) {
        return hasFlag(flags, flag1) && hasFlag(flags, flag2);
    }

    public static boolean hasFlag(int flags, int flag1, int flag2, int flag3) {
        return hasFlag(flags, flag1) && hasFlag(flags, flag2) && hasFlag(flags, flag3);
    }

    public static int removeFlag(int flags, int flag) {
        return flags & ~flag;
    }

    public static Builder builder(ServerLevel level, BlockPos pos, int countUraniumRod) {
        return new Builder(level, pos, countUraniumRod);
    }

    public static final class Builder {
        private final ServerLevel level;
        private final BlockPos pos;
        private final double radius;
        private final int countUraniumRod;

        private Builder(ServerLevel level, BlockPos pos, int countUraniumRod) {
            this.level = level;
            this.pos = pos;
            this.radius = Math.min(calculateExplosionRadius(countUraniumRod), 100D);
            this.countUraniumRod = countUraniumRod;
        }

        private float calculateExplosionRadius(int countUraniumRod) {
            return 10F + countUraniumRod; // Ajuste selon tes besoins
        }

        public void create() {
            ExplosionCore explosion = new ExplosionCore(level, pos, radius);
            explosion.explodeReactorCoreTest();
        }

        public void createOldExplosionType() {
            ExplosionCore explosion = new ExplosionCore(level, pos, radius);

            explosion.tick(countUraniumRod, level, pos);
            explosion.explodeReactorCoreTest();
        }
    }

    public ExplosionCore(Level level, BlockPos pos, double radius) {
        this.level = level;
        this.pos = pos;
        this.radius = radius;
    }

    public void tick(int CountUraniumRod, Level level, BlockPos center) {
        float explosionRadius = calculateExplosionRadius(CountUraniumRod);
        explodeReactorCore(level, center, explosionRadius);
    }

    private float calculateExplosionRadius(int countUraniumRod) {
        return 10F + countUraniumRod; // Ajuste selon tes besoins
    }

    private void explodeReactorCore(Level level, BlockPos center, float radius) {
        int r = (int) Math.ceil(radius);

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos currentPos = center.offset(x, y, z);
                    double distanceSquared = x * x + y * y + z * z;
                    double distance = Math.sqrt(distanceSquared);

                    if (distance <= radius) {
                        BlockState blockState = level.getBlockState(currentPos);
                        if (!blockState.isAir() && !blockState.is(Blocks.BEDROCK)) {
                            double probability = distance / radius;
                            double noise = level.random.nextDouble();

                            if (distance > radius * 0.7 && noise < 0.4) {
                                continue;
                            }

                            level.destroyBlock(currentPos, false);

                            // Ajoute du feu au-dessus des blocs détruits avec une probabilité de 20%
                            if (level.random.nextDouble() < 0.2) { // 20% de chance de feu
                                BlockPos abovePos = currentPos.above();
                                BlockState blockBelow = level.getBlockState(currentPos.below(1));
                                BlockState blockAbove = level.getBlockState(abovePos);
                                CreateNuclear.LOGGER.warn("Fire added at {} | blockAbove.isAir() {} | !blockBelow.isAir() {}", abovePos, blockAbove.isAir(), !blockBelow.isAir());
                                // Vérifie que le bloc au-dessus est de l'air et que le bloc en dessous est solide (pas remplacé par de l'air)
                                level.setBlock(abovePos, Blocks.FIRE.defaultBlockState(), 3);
                                CreateNuclear.LOGGER.warn("Fire added at: " + abovePos);

                            }
                        }
                    }
                }
            }
        }
    }

    private record CrustFilter(Object2IntMap<BlockPos> blocks, BlockPos.MutableBlockPos mpos) implements Predicate<BlockPos> {
        @Override
        public boolean test(BlockPos pos) {
            for (Direction dir : Direction.values()) {
                mpos.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());

                if (!blocks.containsKey(mpos)) {
                    return false;
                }
            }

            return true;
        }
    }

    public static class NukeTask {
        public final BlockPos pos;

        public NukeTask(BlockPos pos) {
            this.pos = pos;
        }

        public int distance(ExplosionCore explosion) {
            int x = explosion.pos.getX() - pos.getX();
            int y = explosion.pos.getY() - pos.getY();
            int z = explosion.pos.getZ() - pos.getZ();
            return x * x + y * y + z * z;
        }

        public int horizontalDistance(ExplosionCore explosion) {
            int x = explosion.pos.getX() - pos.getX();
            int z = explosion.pos.getZ() - pos.getZ();
            return x * x + z * z;
        }

        public int getOrder() {
            return 0;
        }

        public int compare(ExplosionCore explosion, NukeTask o) {
            return distance(explosion) - o.distance(explosion);
        }

        public double group(ExplosionCore explosion, double group) {
            return Math.sqrt(distance(explosion)) / group;
        }

        public void execute(ExplosionCore explosion) {
        }
    }

    public static class BlockModification extends NukeTask {
        public final BlockState state;
        public final int flags;
        public final int neighborUpdates;

        public BlockModification(BlockPos pos, BlockState state, int flags, int neighborUpdates) {
            super(pos);
            this.state = state;
            this.flags = flags;
            this.neighborUpdates = neighborUpdates;
        }

        public BlockModification(BlockPos pos, BlockState state) {
            this(pos, state, 2, 64);
        }

        @Override
        public int getOrder() {
            return state.getBlock() instanceof FireBlock ? 2 : (flags != 2 || neighborUpdates != 0) ? 0 : 1;
        }

        @Override
        public void execute(ExplosionCore explosion) {
            explosion.level.setBlock(pos, state, ((flags & 1) != 0) ? flags : (flags | 0x80), neighborUpdates);
        }
    }

    public static class LightUpdate extends NukeTask {
        private final BlockState old;
        private final BlockState state;
        private final int oldLight;
        private final int oldOpacity;

        public LightUpdate(BlockPos pos, BlockState old, BlockState state, int oldLight, int oldOpacity) {
            super(pos);
            this.old = old;
            this.state = state;
            this.oldLight = oldLight;
            this.oldOpacity = oldOpacity;
        }

        @Override
        public int getOrder() {
            return 10;
        }

        @Override
        public int compare(ExplosionCore explosion, NukeTask o) {
            int i = horizontalDistance(explosion) - o.horizontalDistance(explosion);
            int y1 = pos.getY() - explosion.pos.getY();
            int y2 = o.pos.getY() - explosion.pos.getY();
            return i == 0 ? (y1 - y2) : i;
        }

        @Override
        public double group(ExplosionCore explosion, double group) {
            return Math.sqrt(horizontalDistance(explosion)) / group;
        }

        @Override
        public void execute(ExplosionCore explosion) {
            if (state.useShapeForLightOcclusion() || old.useShapeForLightOcclusion() || state.getLightBlock(explosion.level, pos) != oldOpacity || state.getLightEmission(explosion.level, pos) != oldLight) {
                explosion.level.getProfiler().push("queueCheckLight");
                explosion.level.getLightEngine().checkBlock(pos);
                explosion.level.getProfiler().pop();
            }
        }
    }

    private void explodeReactorCoreTest() {
        long startTime = System.currentTimeMillis();

        int r = (int) Math.ceil(radius);
        double radiusSquared = radius * radius;

        long seed = startTime;
        Random random0 = new Random(seed); // utilisé pour la perturbation globale (forme irrégulière)
        Random random = new Random(seed);  // utilisé pour décisions locales (feu, skip, ...)

        int volume = 0;


        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {

                    // Distance au carré au centre de l'explosion
                    double distanceSquared = x * x + y * y + z * z;

                    // Si le point est dans la sphère
                    if (distanceSquared <= radiusSquared) {
                        volume++;
                    }
                }
            }
        }


        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        Object2IntOpenHashMap<BlockPos> blocks = new Object2IntOpenHashMap<>(volume);
        blocks.defaultReturnValue(0);
        List<BlockPos> crust = new ArrayList<>();

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                mpos.move(pos.getX() + x, 0, pos.getZ() + z);

                boolean blockProtected = false;

                if (!level.isInWorldBounds(mpos)) {
                    continue;
                }

                for (int y = -r; y <= r; y++) {
                    BlockPos currentPos = pos.offset(x, y, z);
                    double distanceSquared = x * x + y * y + z * z;
                    double distance = Math.sqrt(distanceSquared)  / (1D - random.nextDouble() * 0.25D);

                    if (distance <= radius) {
                        mpos.setY(y);

                        if (level.isOutsideBuildHeight(mpos)) {
                            continue;
                        }

                        BlockPos ipos = mpos.immutable();

                        try {
                            BlockState state = level.getBlockState(mpos);
                            if (blockProtected) {
                                blocks.put(ipos, FLAG_IN_EXPLOSION | FLAG_IS_REINFORCED | FLAG_INSIDE);
                            } else if (state.isAir()) {
                                blocks.put(ipos, FLAG_IN_EXPLOSION | FLAG_IS_AIR | FLAG_INSIDE);
                            } else if (state.is(CNBlocks.REINFORCED_GLASS.get()) || state.getDestroySpeed(level, mpos) < 0F) {
                                blocks.put(ipos, FLAG_IN_EXPLOSION | FLAG_IS_REINFORCED | FLAG_INSIDE);
                            } else {
                                blocks.put(ipos, FLAG_IN_EXPLOSION | FLAG_IS_BLOCK | FLAG_INSIDE);
                            }
                        } catch (Exception e) {
                            CreateNuclear.LOGGER.warn("Exception in block checking", e);
                            blocks.put(ipos, FLAG_IN_EXPLOSION | FLAG_IS_REINFORCED | FLAG_INSIDE);
                        }

                        if (distance >= radiusSquared) {
                            crust.add(ipos);
                        }
                    }
                }
            }
        }

        crust.removeIf(new CrustFilter(blocks, mpos));

        List<NukeTask> tasks = new ArrayList<>();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState podzol = Blocks.PODZOL.defaultBlockState();
        BlockState coarseDirt = Blocks.COARSE_DIRT.defaultBlockState();
        BlockState fire = Blocks.FIRE.defaultBlockState();
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();

        if (!hasFlag(blocks.getOrDefault(pos, 0), FLAG_IS_REINFORCED)) {
            tasks.add(new BlockModification(pos, air, 3, Mth.ceil(radius)));
        }

        double step = 0.5D;

        for (BlockPos p : crust) {
            int flags = blocks.getOrDefault(p, 0);

            if (hasFlag(flags, FLAG_IN_EXPLOSION)) {
                blocks.put(p, removeFlag(flags | FLAG_OUTSIDE, FLAG_INSIDE));
            }

            int x0 = pos.getX() - p.getX();
            int y0 = pos.getY() - p.getY();
            int z0 = pos.getZ() - p.getZ();
            int distSq = x0 * x0 + y0 * y0 + z0 * z0;
            double dist = Math.sqrt(distSq);

            int px = 0;
            int py = 0;
            int pz = 0;

            for (double l = 0D; l <= dist; l += step) {
                int x = Mth.floor(x0 * l / dist + 0.5D);
                int y = Mth.floor(y0 * l / dist + 0.5D);
                int z = Mth.floor(z0 * l / dist + 0.5D);

                if (px == x && py == y && pz == z) {
                    continue;
                }

                px = x;
                py = y;
                pz = z;

                mpos.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                int flags1 = blocks.getOrDefault(mpos, 0);

                if (hasFlag(flags1, FLAG_IS_REINFORCED)) {
                    break;
                } else if (hasFlag(flags1, FLAG_IN_EXPLOSION) && !hasFlag(flags1, FLAG_DESTROY)) {
                    BlockPos p1 = mpos.immutable();
                    blocks.put(p1, flags1 | FLAG_DESTROY);
                }
            }
        }

        for (Object2IntMap.Entry<BlockPos> entry : blocks.object2IntEntrySet()) {
            int flags = entry.getIntValue();

            if (hasFlag(flags, FLAG_DESTROY, FLAG_INSIDE, FLAG_IS_BLOCK)) {
                try {
                    BlockPos p = entry.getKey();
                    BlockState state = level.getBlockState(p);
                    int oldLight = state.getLightEmission(level, pos);
                    int oldOpacity = state.getLightBlock(level, pos);

                    tasks.add(new BlockModification(p, air, 2, 0));
                    tasks.add(new LightUpdate(p, state, air, oldLight, oldOpacity));
                } catch (Exception ex) {
                    CreateNuclear.LOGGER.warn("Error while calculating nuclear explosion", ex);
                }
            }
        }

        for (Object2IntMap.Entry<BlockPos> entry : blocks.object2IntEntrySet()) {
            int flags = entry.getIntValue();

            if (hasFlag(flags, FLAG_DESTROY, FLAG_IS_BLOCK, FLAG_OUTSIDE)) {
                try {
                    BlockPos p = entry.getKey();
                    BlockState state = level.getBlockState(p);
                    int oldLight = state.getLightEmission(level, pos);
                    int oldOpacity = state.getLightBlock(level, pos);

                    if (state.getBlock() instanceof GrassBlock) {
                        tasks.add(new BlockModification(p, podzol));
                        tasks.add(new LightUpdate(p, state, podzol, oldLight, oldOpacity));
                    } else if (state.is(BlockTags.DIRT)) {
                        tasks.add(new BlockModification(p, coarseDirt));
                        tasks.add(new LightUpdate(p, state, coarseDirt, oldLight, oldOpacity));
                    } else if (state.is(Tags.Blocks.STONE) || state.is(Tags.Blocks.GRAVEL) || state.is(Tags.Blocks.SAND)) {
                        if (random.nextInt(10) == 0) {
                            BlockState burnt = getBurntBlock(random);
                            tasks.add(new BlockModification(p, burnt));
                            tasks.add(new LightUpdate(p, state, burnt, oldLight, oldOpacity));

                            if (random.nextInt(8) == 0) {
                                BlockPos above = p.above();
                                int aboveFlags = blocks.getOrDefault(above, 0);

                                if (hasFlag(aboveFlags, FLAG_INSIDE) && !hasFlag(aboveFlags, FLAG_IS_REINFORCED)) {
                                    tasks.add(new BlockModification(above, fire, 3, 64));
                                }
                            }
                        } else {
                            tasks.add(new BlockModification(p, cobble));
                            tasks.add(new LightUpdate(p, state, cobble, oldLight, oldOpacity));
                        }
                    } else {
                        tasks.add(new LightUpdate(p, state, state, oldLight, oldOpacity));
                    }
                } catch (Exception ex) {
                    CreateNuclear.LOGGER.warn("Error while calculating nuclear explosion", ex);
                }
            } else if (hasFlag(flags, FLAG_DESTROY, FLAG_IS_AIR, FLAG_OUTSIDE)) {
                try {
                    BlockPos p = entry.getKey();
                    BlockState state = level.getBlockState(p);
                    int oldLight = state.getLightEmission(level, pos);
                    int oldOpacity = state.getLightBlock(level, pos);

                    tasks.add(new LightUpdate(p, state, state, oldLight, oldOpacity));
                } catch (Exception ex) {
                    CreateNuclear.LOGGER.warn("Error while calculating nuclear explosion", ex);
                }
            }
        }

        int modifiedBlocks = 0;

        for (NukeTask task : tasks) {
            if (task instanceof BlockModification) {
                modifiedBlocks++;
            }
        }

        while (!tasks.isEmpty()) {
            tasks.sort(this);
            int highestOrder = tasks.get(0).getOrder();
            List<NukeTask> lowPriority = new ArrayList<>();

            List<List<NukeTask>> lists = new ArrayList<>();

            double group = 1.5;

            for (int i = Mth.ceil(radius / group); i >= 0; i--) {
                lists.add(new ArrayList<>());
            }

            for (NukeTask task : tasks) {
                if (task.getOrder() > highestOrder) {
                    lowPriority.add(task);
                } else {
                    lists.get(Mth.clamp(Mth.floor(task.group(this, group)), 0, lists.size() - 1)).add(task);
                }
            }

            tasks.clear();
            tasks.addAll(lowPriority);
            lists.removeIf(List::isEmpty);

            for (List<NukeTask> list : lists) {
                //                if (!server.isRunning()) {
//                    return;
//                }

//                server.submitAsync(() -> execute(list));
                execute(list, level);
            }
        }
    }

    private void execute(List<NukeTask> list, Level level) {
        boolean blockDrops = level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS);
        level.getGameRules().getRule(GameRules.RULE_DOBLOCKDROPS).set(false, level.getServer());

        for (NukeTask task : list) {
            task.execute(this);
        }

        level.getGameRules().getRule(GameRules.RULE_DOBLOCKDROPS).set(blockDrops, level.getServer());
    }

    private BlockState getBurntBlock(Random random) {
        return switch (random.nextInt(3)) {
            case 0 -> Blocks.MAGMA_BLOCK.defaultBlockState();
            case 1 -> Blocks.BASALT.defaultBlockState();
            case 2 -> CNBlocks.ENRICHING_FIRE.get().defaultBlockState();
            default -> Blocks.BLACKSTONE.defaultBlockState();
        };
    }

    @Override
    public int compare(NukeTask o1, NukeTask o2) {
        int i = o1.getOrder() - o2.getOrder();
        return i == 0 ? o1.compare(this, o2) : i;
    }
}
