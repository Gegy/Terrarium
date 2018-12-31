package net.gegy1000.terrarium.server.world.region;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.gegy1000.terrarium.api.ChunkTracker;
import net.gegy1000.terrarium.server.world.pipeline.ChunkRasterHandler;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.DataSourceHandler;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.RasterDataAccess;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerChunkManagerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.ChunkSaveHandler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RegionGenerationHandler {
    private final TerrariumDataProvider dataProvider;
    private final ChunkRasterHandler chunkRasterHandler;

    private final Map<RegionTilePos, GenerationRegion> regionCache = new HashMap<>();
    private final RegionGenerationDispatcher dispatcher = new OffThreadGenerationDispatcher(this::generate);
    private final DataSourceHandler sourceHandler = new DataSourceHandler();

    private final Object2BooleanMap<ChunkPos> chunkStateMap = new Object2BooleanOpenHashMap<>();

    public RegionGenerationHandler(TerrariumDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.chunkRasterHandler = new ChunkRasterHandler(this, dataProvider);
    }

    public void trackRegions(ServerWorld world, ServerChunkManager chunkManager) {
        Collection<ServerChunkManagerEntry> chunkEntries = ((ChunkTracker) chunkManager).getTrackedEntries();

        Set<ChunkPos> trackedChunks = chunkEntries.stream()
                .map(ServerChunkManagerEntry::getPos)
                .collect(Collectors.toSet());
        Set<ChunkPos> untrackedChunks = this.chunkStateMap.keySet().stream()
                .filter(pos -> !trackedChunks.contains(pos))
                .collect(Collectors.toSet());
        untrackedChunks.forEach(this.chunkStateMap::removeBoolean);

        Collection<RegionTilePos> requiredRegions = this.collectRequiredRegions(world, chunkEntries);
        this.dispatcher.setRequiredRegions(requiredRegions);

        Set<RegionTilePos> untrackedRegions = this.regionCache.keySet().stream()
                .filter(pos -> !requiredRegions.contains(pos))
                .collect(Collectors.toSet());
        untrackedRegions.forEach(this.regionCache::remove);

        Collection<GenerationRegion> completedRegions = this.dispatcher.collectCompletedRegions();
        for (GenerationRegion region : completedRegions) {
            if (region == null) {
                continue;
            }
            this.regionCache.put(region.getPos(), region);
        }
    }

    private Collection<RegionTilePos> collectRequiredRegions(ServerWorld world, Collection<ServerChunkManagerEntry> chunkEntries) {
        Set<RegionTilePos> requiredRegions = new LinkedHashSet<>();

        for (ServerChunkManagerEntry entry : chunkEntries) {
            Chunk chunk = entry.getChunk();
            if (chunk == null) {
                ChunkPos chunkPos = entry.getPos();
                if (this.isChunkSaved(world, chunkPos)) {
                    continue;
                }
                requiredRegions.add(this.getRegionPos(chunkPos.getXStart(), chunkPos.getZStart()));
            }
        }

        return requiredRegions;
    }

    private boolean isChunkSaved(ServerWorld world, ChunkPos pos) {
        if (this.chunkStateMap.containsKey(pos)) {
            return this.chunkStateMap.getBoolean(pos);
        }
        ChunkSaveHandler saveHandler = ((ChunkTracker) world.getChunkManager()).getSaveHandler();
        boolean saved = ChunkSaveStateChecker.isChunkSaved(saveHandler, pos.x, pos.z);
        this.chunkStateMap.put(pos, saved);
        return saved;
    }

    public GenerationRegion get(int blockX, int blockZ) {
        return this.get(this.getRegionPos(blockX, blockZ));
    }

    private RegionTilePos getRegionPos(int blockX, int blockZ) {
        return new RegionTilePos(Math.floorDiv(blockX, GenerationRegion.SIZE), Math.floorDiv(blockZ, GenerationRegion.SIZE));
    }

    public GenerationRegion get(RegionTilePos pos) {
        GenerationRegion cachedRegion = this.regionCache.get(pos);
        if (cachedRegion != null) {
            return cachedRegion;
        }

        GenerationRegion generatedRegion = this.dispatcher.get(pos);
        if (generatedRegion != null) {
            this.regionCache.put(generatedRegion.getPos(), generatedRegion);
            return generatedRegion;
        }

        return this.createDefaultRegion(pos);
    }

    public <T extends RasterDataAccess<V>, V> T fillRaster(RegionComponentType<T> componentType, T result, int originX, int originZ, int width, int height, boolean allowPartial) {
        if (allowPartial && !this.hasRegions(originX, originZ, width, height)) {
            return this.dataProvider.populatePartialData(this, componentType, originX, originZ, width, height);
        }

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

        return result;
    }

    private boolean hasRegions(int originX, int originZ, int width, int height) {
        int minRegionX = Math.floorDiv(originX, GenerationRegion.SIZE);
        int maxRegionX = Math.floorDiv((originX + width), GenerationRegion.SIZE);
        int minRegionY = Math.floorDiv(originZ, GenerationRegion.SIZE);
        int maxRegionY = Math.floorDiv((originZ + height), GenerationRegion.SIZE);

        for (int regionY = minRegionY; regionY <= maxRegionY; regionY++) {
            for (int regionX = minRegionX; regionX <= maxRegionX; regionX++) {
                if (!this.regionCache.containsKey(new RegionTilePos(regionX, regionY))) {
                    return false;
                }
            }
        }

        return true;
    }

    private GenerationRegion generate(RegionTilePos pos) {
        RegionData data = this.dataProvider.populateData(this, pos, GenerationRegion.BUFFERED_SIZE, GenerationRegion.BUFFERED_SIZE);
        return new GenerationRegion(pos, data);
    }

    private GenerationRegion createDefaultRegion(RegionTilePos pos) {
        return new GenerationRegion(pos, this.dataProvider.createDefaultData(GenerationRegion.BUFFERED_SIZE, GenerationRegion.BUFFERED_SIZE));
    }

    public void prepareChunk(int originX, int originZ) {
        this.chunkRasterHandler.fillRasters(originX, originZ);
    }

    public void prepareChunk(int originX, int originZ, Collection<RegionComponentType<?>> components) {
        this.chunkRasterHandler.fillRasters(originX, originZ, components);
    }

    public <T extends RasterDataAccess<V>, V> T getCachedChunkRaster(RegionComponentType<T> componentType) {
        return this.chunkRasterHandler.getChunkRaster(componentType);
    }

    public DataSourceHandler getSourceHandler() {
        return this.sourceHandler;
    }

    public void close() {
        this.dispatcher.close();
        this.sourceHandler.close();
    }
}
