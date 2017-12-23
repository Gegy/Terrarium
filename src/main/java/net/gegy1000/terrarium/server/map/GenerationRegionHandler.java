package net.gegy1000.terrarium.server.map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.map.adapter.CoastlineAdapter;
import net.gegy1000.terrarium.server.map.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.generator.EarthGenerationHandler;
import net.gegy1000.terrarium.server.world.generator.EarthScaleHandler;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GenerationRegionHandler {
    private final TerrariumWorldData worldData;
    private final EarthGenerationHandler generationHandler;
    private final EarthScaleHandler scaleHandler;

    private final int scaledDataSize;

    private final LoadingCache<RegionTilePos, GenerationRegion> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .maximumSize(5)
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

    private final List<RegionAdapter> adapters = new LinkedList<>();

    private final int regionSampleSize = GenerationRegion.SAMPLE_SIZE + 1;

    private final short[] sampledHeights = new short[this.regionSampleSize * this.regionSampleSize];
    private final GlobType[] sampledGlobs = ArrayUtils.defaulted(new GlobType[this.regionSampleSize * this.regionSampleSize], GlobType.NO_DATA);

    public GenerationRegionHandler(TerrariumWorldData worldData, EarthGenerationHandler generationHandler, EarthScaleHandler scaleHandler) {
        this.worldData = worldData;
        this.generationHandler = generationHandler;
        this.scaleHandler = scaleHandler;

        this.scaledDataSize = MathHelper.floor(GenerationRegion.SAMPLE_SIZE * this.generationHandler.getSettings().getFinalScale());

        this.adapters.add(new CoastlineAdapter());
    }

    public void addAdapter(RegionAdapter adapter) {
        this.adapters.add(adapter);
    }

    public boolean removeAdapter(RegionAdapter adapter) {
        return this.adapters.remove(adapter);
    }

    public GenerationRegion get(int globalX, int globalZ) {
        return this.get(new RegionTilePos(Math.floorDiv(globalX, GenerationRegion.SIZE), Math.floorDiv(globalZ, GenerationRegion.SIZE)));
    }

    public GenerationRegion get(RegionTilePos pos) {
        try {
            return this.cache.get(pos);
        } catch (ExecutionException e) {
            Terrarium.LOGGER.error("Failed to retrieve generation region from cache at {}", pos, e);
        }
        return this.createDefaultRegion(pos);
    }

    private GenerationRegion generate(RegionTilePos pos) {
        this.generationHandler.initializeSeed(pos);

        Coordinate minCoordinate = pos.getMinCoordinate(this.generationHandler.getSettings());
        Coordinate maxCoordinate = pos.getMaxCoordinate(this.generationHandler.getSettings()).add(2.0, 2.0);
        OverpassTileAccess overpassTile = this.worldData.getOverpassSource().sampleArea(minCoordinate, maxCoordinate);

        short[] heights = this.generateHeights(minCoordinate, maxCoordinate);
        GlobType[] globcover = this.generateGlobcover(overpassTile, minCoordinate, maxCoordinate);

        return new GenerationRegion(pos, minCoordinate, this.scaledDataSize, heights, globcover);
    }

    private short[] generateHeights(Coordinate minCoordinate, Coordinate maxCoordinate) {
        this.worldData.getHeightSource().sampleArea(this.sampledHeights, minCoordinate, maxCoordinate);

        short[] resultHeights = new short[this.scaledDataSize * this.scaledDataSize];
        this.scaleHandler.scaleHeightRegion(resultHeights, this.sampledHeights, this.regionSampleSize, this.regionSampleSize, this.scaledDataSize, this.scaledDataSize);

        return resultHeights;
    }

    private GlobType[] generateGlobcover(OverpassTileAccess overpassTile, Coordinate minCoordinate, Coordinate maxCoordinate) {
        this.worldData.getGlobSource().sampleArea(this.sampledGlobs, minCoordinate, maxCoordinate);

        GlobType[] resultGlobs = ArrayUtils.defaulted(new GlobType[this.scaledDataSize * this.scaledDataSize], GlobType.NO_DATA);
        this.scaleHandler.scaleGlobRegion(resultGlobs, this.sampledGlobs, this.regionSampleSize, this.regionSampleSize, this.scaledDataSize, this.scaledDataSize);

        int originX = MathHelper.floor(minCoordinate.getBlockX());
        int originZ = MathHelper.floor(minCoordinate.getBlockZ());
        for (RegionAdapter adapter : this.adapters) {
            adapter.adaptGlobcover(this.generationHandler.getSettings(), overpassTile, resultGlobs, originX, originZ, this.scaledDataSize, this.scaledDataSize);
        }

        return resultGlobs;
    }

    private GenerationRegion createDefaultRegion(RegionTilePos pos) {
        Coordinate minCoordinate = pos.getMinCoordinate(this.generationHandler.getSettings());
        short[] heights = new short[this.scaledDataSize * this.scaledDataSize];
        GlobType[] globcover = ArrayUtils.defaulted(new GlobType[this.scaledDataSize * this.scaledDataSize], GlobType.NO_DATA);
        return new GenerationRegion(pos, minCoordinate, this.scaledDataSize, heights, globcover);
    }
}
