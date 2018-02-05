package net.gegy1000.terrarium.server.map.system.chunk;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.GenerationRegion;
import net.gegy1000.terrarium.server.map.GenerationRegionHandler;
import net.gegy1000.terrarium.server.map.source.raster.ShortRasterDataAccess;
import net.gegy1000.terrarium.server.map.system.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.world.World;

public class HeightChunkDataProvider implements ChunkDataProvider<int[]> {
    private final EarthGenerationSettings settings;

    private final RegionComponentType<? extends ShortRasterDataAccess> heightComponent;

    private final int[] heightmap = new int[256];

    public HeightChunkDataProvider(EarthGenerationSettings settings, RegionComponentType<? extends ShortRasterDataAccess> heightComponent) {
        this.settings = settings;
        this.heightComponent = heightComponent;
    }

    @Override
    public void populate(GenerationRegionHandler regionHandler, World world, int originX, int originZ) {
        double terrainHeightScale = this.settings.terrainHeightScale * this.settings.worldScale;
        int maxWorldHeight = world.getHeight();

        try {
            for (int localZ = 0; localZ < 16; localZ++) {
                int blockZ = originZ + localZ;

                for (int localX = 0; localX < 16; localX++) {
                    int blockX = originX + localX;

                    GenerationRegion region = regionHandler.get(blockX, blockZ);
                    ShortRasterDataAccess heightTile = region.getData().get(this.heightComponent);

                    if (heightTile != null) {
                        short height = heightTile.getShort(blockX - region.getMinX(), blockZ - region.getMinZ());
                        short scaled = (short) (height * terrainHeightScale);

                        if (height >= 0 && scaled < 1) {
                            scaled = 1;
                        }

                        this.heightmap[localX + localZ * 16] = Math.min(scaled + this.settings.heightOffset, maxWorldHeight - 1);
                    }
                }
            }
        } catch (Exception e) {
            Terrarium.LOGGER.error("Failed to populate heightmap for {}, {}", originX, originZ, e);
        }
    }

    @Override
    public int[] getResultStore() {
        return this.heightmap;
    }
}
