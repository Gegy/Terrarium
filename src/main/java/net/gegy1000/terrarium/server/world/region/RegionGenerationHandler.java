package net.gegy1000.terrarium.server.world.region;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.ChunkRasterHandler;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
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

    private final GenerationSettings settings;
    private final TerrariumDataProvider dataSystem;
    private final ChunkRasterHandler chunkRasterHandler;

    private final Coordinate bufferedRegionSize;

    private final Map<RegionTilePos, GenerationRegion> regionCache = new HashMap<>();
    private final RegionGenerationDispatcher dispatcher = new OffThreadGenerationDispatcher(this::generate);

    static {
        try {
            chunkMapEntriesField = ReflectionHelper.findField(PlayerChunkMap.class, "entries", "field_111193_e");
        } catch (ReflectionHelper.UnableToFindFieldException e) {
            Terrarium.LOGGER.error("Failed to find chunk entries field", e);
        }
    }

    public RegionGenerationHandler(GenerationSettings settings, TerrariumDataProvider dataSystem) {
        this.settings = settings;
        this.dataSystem = dataSystem;
        this.chunkRasterHandler = new ChunkRasterHandler(this, dataSystem);

        this.bufferedRegionSize = Coordinate.fromBlock(GenerationRegion.BUFFERED_SIZE, GenerationRegion.BUFFERED_SIZE);
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

    public void close() {
        this.dispatcher.close();
    }
}
