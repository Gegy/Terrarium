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

    private final int sampledDataSize;

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

    private final short[] sampledHeights;
    private final GlobType[] sampledGlobs;

    public GenerationRegionHandler(TerrariumWorldData worldData, EarthGenerationHandler generationHandler, EarthScaleHandler scaleHandler) {
        this.worldData = worldData;
        this.generationHandler = generationHandler;
        this.scaleHandler = scaleHandler;

        this.sampledDataSize = MathHelper.ceil(GenerationRegion.SIZE * this.generationHandler.getSettings().getInverseScale()) + 1;
        this.sampledHeights = new short[this.sampledDataSize * this.sampledDataSize];
        this.sampledGlobs = ArrayUtils.defaulted(new GlobType[this.sampledDataSize * this.sampledDataSize], GlobType.NO_DATA);

        this.adapters.add(new CoastlineAdapter());
    }

    public void addAdapter(RegionAdapter adapter) {
        this.adapters.add(adapter);
    }

    public boolean removeAdapter(RegionAdapter adapter) {
        return this.adapters.remove(adapter);
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

    private GenerationRegion generate(RegionTilePos pos) {
        this.generationHandler.initializeSeed(pos);

        Coordinate minCoordinate = pos.getMinCoordinate(this.generationHandler.getSettings());
        Coordinate maxCoordinate = pos.getMaxCoordinate(this.generationHandler.getSettings()).add(1.0, 1.0);
        OverpassTileAccess overpassTile = this.worldData.getOverpassSource().sampleArea(minCoordinate, maxCoordinate);

        short[] heights = this.generateHeights(minCoordinate, maxCoordinate);
        GlobType[] globcover = this.generateGlobcover(overpassTile, minCoordinate, maxCoordinate);

        RegionData data = new RegionData(heights, globcover, overpassTile);

        int originX = MathHelper.floor(minCoordinate.getBlockX());
        int originZ = MathHelper.floor(minCoordinate.getBlockZ());
        for (RegionAdapter adapter : this.adapters) {
            try {
                adapter.adapt(this.generationHandler.getSettings(), data, originX, originZ, GenerationRegion.SIZE, GenerationRegion.SIZE);
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to run adapter {}", adapter, e);
            }
        }

        return new GenerationRegion(pos, data);
    }

    private short[] generateHeights(Coordinate minCoordinate, Coordinate maxCoordinate) {
        this.worldData.getHeightSource().sampleArea(this.sampledHeights, minCoordinate, maxCoordinate);

        short[] resultHeights = new short[GenerationRegion.SIZE * GenerationRegion.SIZE];
        this.scaleHandler.scaleHeightRegion(resultHeights, this.sampledHeights, this.sampledDataSize, this.sampledDataSize, GenerationRegion.SIZE, GenerationRegion.SIZE);

        return resultHeights;
    }

    private GlobType[] generateGlobcover(OverpassTileAccess overpassTile, Coordinate minCoordinate, Coordinate maxCoordinate) {
        this.worldData.getGlobSource().sampleArea(this.sampledGlobs, minCoordinate, maxCoordinate);

        GlobType[] resultGlobs = ArrayUtils.defaulted(new GlobType[GenerationRegion.SIZE * GenerationRegion.SIZE], GlobType.NO_DATA);
        this.scaleHandler.scaleGlobRegion(resultGlobs, this.sampledGlobs, this.sampledDataSize, this.sampledDataSize, GenerationRegion.SIZE, GenerationRegion.SIZE);

        return resultGlobs;
    }

    private GenerationRegion createDefaultRegion(RegionTilePos pos) {
        short[] heights = new short[GenerationRegion.SIZE * GenerationRegion.SIZE];
        GlobType[] globcover = ArrayUtils.defaulted(new GlobType[GenerationRegion.SIZE * GenerationRegion.SIZE], GlobType.NO_DATA);
        return new GenerationRegion(pos, new RegionData(heights, globcover, new OverpassTileAccess()));
    }
}
