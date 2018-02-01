package net.gegy1000.terrarium.server.world.generator.debug;

import net.gegy1000.terrarium.server.world.generator.CoveredBiomeProvider;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class CoverDebugBiomeProvider extends CoveredBiomeProvider {
    public CoverDebugBiomeProvider(World world) {
        super(world);
    }

    @Override
    protected void populateChunk(Biome[] biomes, int x, int z) {
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                DebugMap.DebugCover cover = DebugMap.getCover(localX + x, localZ + z);
                biomes[localX + localZ * 16] = cover.getCoverType().getBiome(cover.getZone());
            }
        }
    }
}
