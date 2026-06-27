package net.nuclearteam.createnuclear.infrastructure.worldgen.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

public class OriginalBiomeLookup {
    private OriginalBiomeLookup() {}

    public static Holder<Biome> resolve(ServerLevel level, BlockPos pos) {
        if (!PersistentIrradiatedZones.get(level).isInsideAnyZone(pos)) {
            return level.getBiome(pos);
        }

        return level.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(
                QuartPos.fromBlock(pos.getX()),
                QuartPos.fromBlock(pos.getY()),
                QuartPos.fromBlock(pos.getZ()),
                level.getChunkSource().randomState().sampler()
        );
    }
}
