package net.nuclearteam.createnuclear.infrastructure.worldgen.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;

public final class BiomeIrradiationService {
    public static void circularArea(ServerLevel serverLevel, BlockPos center, ResourceKey<Biome> defaultTarget, int radius) {
        Registry<Biome> biomeRegistry = serverLevel.registryAccess().registryOrThrow(Registries.BIOME);

        Holder<Biome> currentAtCenter = serverLevel.getBiome(center);
        ResourceKey<Biome> targetAtCenter = BiomeIrradiationMappings.resolveTarget(currentAtCenter, defaultTarget);
        if (currentAtCenter.is(targetAtCenter)) {
            return;
        }

        int minX = center.getX() - radius;
        int maxX = center.getX() + radius;
        int minZ = center.getZ() - radius;
        int maxZ = center.getZ() + radius;

        double radiusSq = radius * radius;
        BiomeResolver resolver = createCircularResolver(biomeRegistry, center, radiusSq, defaultTarget, serverLevel);

        List<ChunkAccess> chunks = new ArrayList<>();
        for (int cz = SectionPos.blockToSectionCoord(minZ); cz < SectionPos.blockToSectionCoord(maxZ); ++cz) {
            for (int cx = SectionPos.blockToSectionCoord(minX); cx < SectionPos.blockToSectionCoord(maxX); ++cx) {
                ChunkAccess chunkAccess = serverLevel.getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunkAccess != null) {
                    chunkAccess.fillBiomesFromNoise(resolver, serverLevel.getChunkSource().randomState().sampler());
                    chunkAccess.setUnsaved(true);
                    chunks.add(chunkAccess);
                }
            }
        }

        serverLevel.getChunkSource().chunkMap.resendBiomesForChunks(chunks);
    }

    private static BiomeResolver createCircularResolver(Registry<Biome> biomeRegistry, BlockPos center, double radiusSq, ResourceKey<Biome> defaultTarget, ServerLevel level) {
        return (x, y, z, noise) -> {
            int blockX = x << 2;
            int blockZ = z << 2;

            double distX = blockX - center.getX();
            double distZ = blockZ - center.getZ();

            Holder<Biome> current = level.getBiome(new BlockPos(blockX, y << 2, blockZ));

            if ((distX * distX) + (distZ * distZ) <= radiusSq) {
                ResourceKey<Biome> target = BiomeIrradiationMappings.resolveTarget(current, defaultTarget);

                return biomeRegistry.getHolderOrThrow(target);
            }

            return current;
        };
    }
}
