package net.gegy1000.terrarium.server.world.pipeline.adapter.debug;

import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.region.GenerationRegion;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class DebugRegionBorderAdapter implements RegionAdapter {
    private final RegionComponentType<BiomeRasterTile> biomeComponent;

    public DebugRegionBorderAdapter(RegionComponentType<BiomeRasterTile> biomeComponent) {
        this.biomeComponent = biomeComponent;
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        BiomeRasterTile coverTile = data.getOrExcept(this.biomeComponent);

        Biome[] biomes = coverTile.getData();

        int minX = GenerationRegion.BUFFER;
        int minZ = GenerationRegion.BUFFER;
        int maxX = width - GenerationRegion.BUFFER;
        int maxZ = height - GenerationRegion.BUFFER;
        for (int localZ = minZ; localZ < maxZ; localZ++) {
            for (int localX = minX; localX < maxX; localX++) {
                if (localX == minX || localX == maxX - 1 || localZ == minZ || localZ == maxZ - 1) {
                    biomes[localX + localZ * width] = Biomes.THE_END;
                }
            }
        }
    }
}
