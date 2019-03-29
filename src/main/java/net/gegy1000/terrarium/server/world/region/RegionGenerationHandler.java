package net.gegy1000.terrarium.server.world.region;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.gegy1000.cubicglue.CubicGlue;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.world.chunk.tracker.ChunkTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.ChunkTrackerHooks;
import net.gegy1000.terrarium.server.world.chunk.tracker.ColumnTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.CubeTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.FallbackTrackerAccess;
import net.gegy1000.terrarium.server.world.chunk.tracker.TrackedColumn;
import net.gegy1000.terrarium.server.world.pipeline.ChunkRasterHandler;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.RasterData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
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

    private final ChunkTrackerAccess chunkTrackerAccess;
    private final Object2BooleanMap<ChunkPos> chunkStateMap = new Object2BooleanOpenHashMap<>();

    public RegionGenerationHandler(World world, TerrariumDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.chunkRasterHandler = new ChunkRasterHandler(this, dataProvider);

        this.chunkTrackerAccess = createTrackerAccess(world);
    }

    private static ChunkTrackerAccess createTrackerAccess(World world) {
        if (!(world instanceof WorldServer)) {
            return new FallbackTrackerAccess();
        }
        if (CubicGlue.isCubic(world)) {
            return createCubeTracker((WorldServer) world);
        }
        return new ColumnTrackerAccess((WorldServer) world);
    }

    private static ChunkTrackerAccess createCubeTracker(WorldServer world) {
        return new CubeTrackerAccess(world);
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

    public void trackRegions(WorldServer world) {
        Collection<TrackedColumn> columnEntries = this.chunkTrackerAccess.getSortedTrackedColumns();
        ChunkTrackerHooks chunkHooks = world.getCapability(TerrariumCapabilities.chunkHooksCapability, null);

        Collection<RegionTilePos> requiredRegions = this.collectRequiredRegions(world, chunkHooks, columnEntries);
        this.dispatcher.setRequiredRegions(requiredRegions);

        this.unpauseChunks(chunkHooks);

        this.trackRegions(requiredRegions);
    }

    private void unpauseChunks(ChunkTrackerHooks chunkHooks) {
        if (chunkHooks != null) {
            Set<ChunkPos> pausedChunks = chunkHooks.getPausedChunks();
            if (!pausedChunks.isEmpty()) {
                Set<ChunkPos> unhooked = pausedChunks.stream()
                        .filter(chunkPos -> {
                            RegionTilePos regionPos = this.getRegionPos(chunkPos.getXStart(), chunkPos.getZStart());
                            return this.regionCache.containsKey(regionPos);
                        })
                        .collect(Collectors.toSet());
                unhooked.forEach(chunkHooks::unpauseChunk);
            }
        }
    }

    private Collection<RegionTilePos> collectRequiredRegions(WorldServer world, ChunkTrackerHooks chunkHooks, Collection<TrackedColumn> chunkEntries) {
        Set<RegionTilePos> requiredRegions = new LinkedHashSet<>();

        for (TrackedColumn entry : chunkEntries) {
            if (entry.isQueued()) {
                ChunkPos chunkPos = entry.getPos();
                if (this.computeChunkSaved(world, chunkPos)) {
                    continue;
                }
                RegionTilePos regionPos = this.getRegionPos(chunkPos.getXStart(), chunkPos.getZStart());
                if (chunkHooks != null && !this.regionCache.containsKey(regionPos)) {
                    chunkHooks.pauseChunk(chunkPos);
                }
                requiredRegions.add(regionPos);
            }
        }

        return requiredRegions;
    }

    private boolean computeChunkSaved(WorldServer world, ChunkPos pos) {
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

    private Collection<RegionTilePos> getRegions(int minX, int minZ, int maxX, int maxZ) {
        RegionTilePos minRegion = this.getRegionPos(minX, minZ);
        RegionTilePos maxRegion = this.getRegionPos(maxX, maxZ);

        int width = maxRegion.getTileX() - minRegion.getTileX() + 1;
        int height = maxRegion.getTileZ() - minRegion.getTileZ() + 1;
        Collection<RegionTilePos> regions = new ArrayList<>(width * height);
        for (int regionZ = minRegion.getTileZ(); regionZ <= maxRegion.getTileZ(); regionZ++) {
            for (int regionX = minRegion.getTileX(); regionX <= maxRegion.getTileX(); regionX++) {
                regions.add(new RegionTilePos(regionX, regionZ));
            }
        }

        return regions;
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

    public <T extends RasterData<V>, V> void fillRaster(RegionComponentType<T> componentType, T result, int originX, int originZ) {
        int width = result.getWidth();
        int height = result.getHeight();

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

    public <T extends RasterData<V>, V> T computePartialRaster(RegionComponentType<T> componentType, int originX, int originZ, int width, int height) {
        if (!this.hasRegions(originX, originZ, width, height)) {
            return this.dataProvider.populatePartialData(componentType, originX, originZ, width, height);
        } else {
            T result = componentType.createDefaultData(width, height);
            this.fillRaster(componentType, result, originX, originZ);
            return result;
        }
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
        RegionData data = this.dataProvider.populateData(pos);
        return new GenerationRegion(pos, data);
    }

    private GenerationRegion createDefaultRegion(RegionTilePos pos) {
        return new GenerationRegion(pos, this.dataProvider.createDefaultData(GenerationRegion.BUFFERED_SIZE, GenerationRegion.BUFFERED_SIZE));
    }

    public void prepareChunk(int originX, int originZ) {
        this.chunkRasterHandler.fillRasters(originX, originZ);
    }

    public void prepareChunkPartial(int originX, int originZ, Collection<RegionComponentType<?>> components) {
        this.chunkRasterHandler.fillRastersPartial(originX, originZ, components);
    }

    public <T extends RasterData<V>, V> T getCachedChunkRaster(RegionComponentType<T> componentType) {
        return this.chunkRasterHandler.getChunkRaster(componentType);
    }

    public void close() {
        this.dispatcher.close();
    }
}
