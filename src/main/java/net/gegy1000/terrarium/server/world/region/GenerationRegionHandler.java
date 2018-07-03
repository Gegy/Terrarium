package net.gegy1000.terrarium.server.world.region;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.ChunkRasterHandler;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.RasterDataAccess;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GenerationRegionHandler {
    private final GenerationSettings settings;
    private final TerrariumDataProvider dataSystem;
    private final ChunkRasterHandler chunkRasterHandler;

    private final Coordinate bufferedRegionSize;

    private final LoadingCache<RegionTilePos, GenerationRegion> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .maximumSize(12)
            .build(new CacheLoader<RegionTilePos, GenerationRegion>() {
                @Override
                public GenerationRegion load(RegionTilePos key) {
                    try {
                        return GenerationRegionHandler.this.generate(key);
                    } catch (Exception e) {
                        Terrarium.LOGGER.error("Failed to load generation region at {}", key, e);
                    }
                    return GenerationRegionHandler.this.createDefaultRegion(key);
                }
            });

    public GenerationRegionHandler(GenerationSettings settings, TerrariumDataProvider dataSystem) {
        this.settings = settings;
        this.dataSystem = dataSystem;
        this.chunkRasterHandler = new ChunkRasterHandler(this, dataSystem);

        this.bufferedRegionSize = Coordinate.fromBlock(GenerationRegion.BUFFERED_SIZE, GenerationRegion.BUFFERED_SIZE);
    }

    public GenerationRegion get(int blockX, int blockZ) {
        return this.get(new RegionTilePos(Math.floorDiv(blockX, GenerationRegion.SIZE), Math.floorDiv(blockZ, GenerationRegion.SIZE)));
    }

    public GenerationRegion get(RegionTilePos pos) {
        try {
            return this.cache.get(pos);
        } catch (ExecutionException e) {
            Terrarium.LOGGER.error("Failed to retrieve generation region from cache at {}", pos, e);
        }
        return this.createDefaultRegion(pos);
    }

    public <T extends RasterDataAccess<V>, V> void fillRaster(RegionComponentType<T> componentType, T result, int originX, int originZ, int width, int height) {
        for (int localZ = 0; localZ < height; localZ++) {
            int blockZ = originZ + localZ;

            for (int localX = 0; localX < width; localX++) {
                int blockX = originX + localX;

                GenerationRegion region = this.get(blockX, blockZ);
                T dataTile = region.getData().getOrExcept(componentType);

                V value = dataTile.get(blockX - region.getMinX(), blockZ - region.getMinZ());
                result.set(localX, localZ, value);
            }
        }
    }

    private GenerationRegion generate(RegionTilePos pos) {
        RegionData data = this.dataSystem.populateData(this.settings, pos, this.bufferedRegionSize, GenerationRegion.BUFFERED_SIZE, GenerationRegion.BUFFERED_SIZE);
        return new GenerationRegion(pos, data);
    }

    private GenerationRegion createDefaultRegion(RegionTilePos pos) {
        return new GenerationRegion(pos, new RegionData(Collections.emptyMap()));
    }

    public void prepareChunk(int originX, int originZ) {
        this.chunkRasterHandler.fillRasters(originX, originZ);
    }

    public <T extends RasterDataAccess<V>, V> T getCachedChunkRaster(RegionComponentType<T> componentType) {
        return this.chunkRasterHandler.getChunkRaster(componentType);
    }
}
