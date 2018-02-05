package net.gegy1000.terrarium.server.map.system.chunk;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.GenerationRegion;
import net.gegy1000.terrarium.server.map.GenerationRegionHandler;
import net.gegy1000.terrarium.server.map.source.raster.ByteRasterDataAccess;
import net.gegy1000.terrarium.server.map.system.component.RegionComponentType;
import net.minecraft.world.World;

public class SlopeChunkDataProvider implements ChunkDataProvider<byte[]> {
    private final RegionComponentType<? extends ByteRasterDataAccess> slopeComponent;

    private final byte[] slopeMap = new byte[256];

    public SlopeChunkDataProvider(RegionComponentType<? extends ByteRasterDataAccess> slopeComponent) {
        this.slopeComponent = slopeComponent;
    }

    @Override
    public void populate(GenerationRegionHandler regionHandler, World world, int originX, int originZ) {
        try {
            for (int localZ = 0; localZ < 16; localZ++) {
                int blockZ = originZ + localZ;

                for (int localX = 0; localX < 16; localX++) {
                    int blockX = originX + localX;

                    GenerationRegion region = regionHandler.get(blockX, blockZ);
                    ByteRasterDataAccess slopeTile = region.getData().get(this.slopeComponent);

                    if (slopeTile != null) {
                        this.slopeMap[localX + localZ * 16] = slopeTile.getByte(blockX - region.getMinX(), blockZ - region.getMinZ());
                    }
                }
            }
        } catch (Exception e) {
            Terrarium.LOGGER.error("Failed to populate slope map for {}, {}", originX, originZ, e);
        }
    }

    @Override
    public byte[] getResultStore() {
        return this.slopeMap;
    }
}
