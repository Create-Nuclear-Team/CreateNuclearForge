package net.nuclearteam.createnuclear.content.multiblock.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.nuclearteam.createnuclear.CreateNuclear;

public class ExplosionCore {

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
}
