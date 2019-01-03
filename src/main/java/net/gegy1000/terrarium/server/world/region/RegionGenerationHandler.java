package net.gegy1000.terrarium.server.world.region;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.gegy1000.terrarium.server.world.chunk.PlayerChunkMapHooks;
import net.gegy1000.terrarium.server.world.pipeline.ChunkRasterHandler;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.DataSourceHandler;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.RasterDataAccess;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
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

    public void enqueueArea(BlockPos min, BlockPos max) {
        RegionTilePos minRegion = this.getRegionPos(min.getX(), min.getZ());
        RegionTilePos maxRegion = this.getRegionPos(max.getX(), max.getZ());

        int width = maxRegion.getTileX() - minRegion.getTileX() + 1;
        int height = maxRegion.getTileZ() - minRegion.getTileZ() + 1;
        Collection<RegionTilePos> regions = new ArrayList<>(width * height);
        for (int regionZ = minRegion.getTileZ(); regionZ <= maxRegion.getTileZ(); regionZ++) {
            for (int regionX = minRegion.getTileX(); regionX <= maxRegion.getTileX(); regionX++) {
                regions.add(new RegionTilePos(regionX, regionZ));
            }
        }

        for (RegionTilePos region : regions) {
            this.dataProvider.enqueueData(this.sourceHandler, region);
        }

        this.trackRegions(regions);
    }

    public void trackRegions(Collection<RegionTilePos> regions) {
        this.dispatcher.setRequiredRegions(regions);

        Set<RegionTilePos> untrackedRegions = this.regionCache.keySet().stream()
                .filter(pos -> !regions.contains(pos))
                .collect(Collectors.toSet());
        untrackedRegions.forEach(pos -> {
            this.regionCache.remove(pos);
            this.dispatcher.cancel(pos);
        });

        Collection<GenerationRegion> completedRegions = this.dispatcher.collectCompletedRegions();
        for (GenerationRegion region : completedRegions) {
            if (region == null) {
                continue;
            }
            this.regionCache.put(region.getPos(), region);
        }
    }

    public void trackRegions(WorldServer world, PlayerChunkMap chunkTracker) {
        List<PlayerChunkMapEntry> chunkEntries = PlayerChunkMapHooks.getSortedChunkEntries(chunkTracker);

        Set<ChunkPos> trackedChunks = chunkEntries.stream()
                .map(PlayerChunkMapEntry::getPos)
                .collect(Collectors.toSet());
        Set<ChunkPos> untrackedChunks = this.chunkStateMap.keySet().stream()
                .filter(pos -> !trackedChunks.contains(pos))
                .collect(Collectors.toSet());
        untrackedChunks.forEach(this.chunkStateMap::remove);

        Collection<RegionTilePos> requiredRegions = this.collectRequiredRegions(world, chunkTracker, chunkEntries);

        if (chunkTracker instanceof PlayerChunkMapHooks.Wrapper) {
            PlayerChunkMapHooks.Wrapper hookedTracker = (PlayerChunkMapHooks.Wrapper) chunkTracker;
            Set<ChunkPos> hookedChunks = hookedTracker.getHookedChunks();
            if (!hookedChunks.isEmpty()) {
                Set<ChunkPos> unhooked = hookedChunks.stream()
                        .filter(chunkPos -> {
                            RegionTilePos regionPos = this.getRegionPos(chunkPos.getXStart(), chunkPos.getZStart());
                            return this.regionCache.containsKey(regionPos);
                        })
                        .collect(Collectors.toSet());
                unhooked.forEach(hookedTracker::unhookChunk);
            }
        }

        this.trackRegions(requiredRegions);
    }

    private Collection<RegionTilePos> collectRequiredRegions(WorldServer world, PlayerChunkMap chunkTracker, List<PlayerChunkMapEntry> chunkEntries) {
        Set<RegionTilePos> requiredRegions = new LinkedHashSet<>();

        for (PlayerChunkMapEntry entry : chunkEntries) {
            Chunk chunk = entry.getChunk();
            if (chunk == null) {
                ChunkPos chunkPos = entry.getPos();
                if (this.isChunkSaved(world, chunkPos)) {
                    continue;
                }
                RegionTilePos regionPos = this.getRegionPos(chunkPos.getXStart(), chunkPos.getZStart());
                if (!this.regionCache.containsKey(regionPos)) {
                    if (chunkTracker instanceof PlayerChunkMapHooks.Wrapper) {
                        ((PlayerChunkMapHooks.Wrapper) chunkTracker).hookChunk(chunkPos);
                    }
                }
                requiredRegions.add(regionPos);
            }
        }

        return requiredRegions;
    }

    private boolean isChunkSaved(WorldServer world, ChunkPos pos) {
        if (this.chunkStateMap.containsKey(pos)) {
            return this.chunkStateMap.get(pos);
        }
        boolean saved = world.getChunkProvider().chunkLoader.isChunkGeneratedAt(pos.x, pos.z);
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
        this.dataProvider.enqueueData(this.sourceHandler, pos);
        RegionData data = this.dataProvider.populateData(this.sourceHandler, pos);

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
