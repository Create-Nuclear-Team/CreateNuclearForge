package net.nuclearteam.createnuclear.content.multiblock.core;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class ExplosionCore {

     public static final int FLAG_IN_EXPLOSION = 1 << 0;
     public static final int FLAG_IS_REINFORCED = 1 << 1;
     public static final int FLAG_DESTROY = 1 << 2;
     public static final int FLAG_IS_AIR = 1 << 3;
     public static final int FLAG_IS_BLOCK = 1 << 4;
     public static final int FLAG_INSIDE = 1 << 5;
     public static final int FLAG_OUTSIDE = 1 << 6;

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

    private void explodeReactorCoreTest(Level level, BlockPos center, float radius) {
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
                mpos.move(center.getX() + x, 0, center.getZ() + z);

                boolean blockProtected = false;

                if (!level.isInWorldBounds(mpos)) {
                    continue;
                }

                for (int y = -r; y <= r; y++) {
                    BlockPos currentPos = center.offset(x, y, z);
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

//        crust.removeIf()
    }
}
