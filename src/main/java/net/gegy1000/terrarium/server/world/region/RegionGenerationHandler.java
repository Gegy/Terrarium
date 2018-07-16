package net.gegy1000.terrarium.server.world.region;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.pipeline.ChunkRasterHandler;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.DataSourceHandler;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.RasterDataAccess;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RegionGenerationHandler {
    private static Field chunkMapEntriesField;

    private final TerrariumDataProvider dataProvider;
    private final ChunkRasterHandler chunkRasterHandler;

    private final Map<RegionTilePos, GenerationRegion> regionCache = new HashMap<>();
    private final RegionGenerationDispatcher dispatcher = new OffThreadGenerationDispatcher(this::generate);
    private final DataSourceHandler sourceHandler = new DataSourceHandler();

    static {
        try {
            chunkMapEntriesField = ReflectionHelper.findField(PlayerChunkMap.class, "entries", "field_111193_e");
        } catch (ReflectionHelper.UnableToFindFieldException e) {
            Terrarium.LOGGER.error("Failed to find chunk entries field", e);
        }
    }

    public RegionGenerationHandler(TerrariumDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.chunkRasterHandler = new ChunkRasterHandler(this, dataProvider);
    }

    public void trackRegions(PlayerChunkMap chunkTracker) {
        Collection<RegionTilePos> requiredRegions = this.collectRequiredRegions(chunkTracker);
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

    @SuppressWarnings("unchecked")
    private Collection<RegionTilePos> collectRequiredRegions(PlayerChunkMap chunkTracker) {
        if (chunkMapEntriesField != null) {
            try {
                Set<RegionTilePos> requiredRegions = new LinkedHashSet<>();

                // TODO: This might include already generated (just not loaded) chunks. Make use of AnvilChunkLoader#isChunkGeneratedAt and cache!
                List<PlayerChunkMapEntry> entries = (List<PlayerChunkMapEntry>) chunkMapEntriesField.get(chunkTracker);

                List<PlayerChunkMapEntry> sortedEntries = new ArrayList<>(entries);
                sortedEntries.sort(Comparator.comparingDouble(PlayerChunkMapEntry::getClosestPlayerDistance));

                for (PlayerChunkMapEntry entry : entries) {
                    if (entry.getChunk() == null) {
                        ChunkPos chunkPos = entry.getPos();
                        requiredRegions.add(this.getRegionPos(chunkPos.getXStart(), chunkPos.getZStart()));
                    }
                }

                return requiredRegions;
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to get player chunk entries", e);
            }
        }

        return Collections.emptySet();
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
