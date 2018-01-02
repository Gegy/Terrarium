package net.gegy1000.terrarium.server.map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.map.adapter.CoastlineAdapter;
import net.gegy1000.terrarium.server.map.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.map.adapter.WaterFlattenAdapter;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.source.osm.DetailedOverpassSource;
import net.gegy1000.terrarium.server.map.source.osm.GeneralOverpassSource;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
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

    private final Coordinate regionSize;

    private final int heightSampleSize;
    private final short[] sampledHeights;

    private final int globSampleSize;
    private final GlobType[] sampledGlobs;

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

    public GenerationRegionHandler(TerrariumWorldData worldData, EarthGenerationHandler generationHandler, EarthScaleHandler scaleHandler) {
        this.worldData = worldData;
        this.generationHandler = generationHandler;
        this.scaleHandler = scaleHandler;

        this.regionSize = Coordinate.fromBlock(generationHandler.getSettings(), GenerationRegion.SIZE, GenerationRegion.SIZE);
        if (this.regionSize.getGlobalX() != this.regionSize.getGlobalZ()) {
            throw new IllegalStateException("Cannot generate region where width != height");
        }

        this.heightSampleSize = MathHelper.ceil(this.regionSize.getGlobalX() + 1);
        this.globSampleSize = MathHelper.ceil(this.regionSize.getGlobX());

        this.sampledHeights = new short[this.heightSampleSize * this.heightSampleSize];
        this.sampledGlobs = ArrayUtils.defaulted(new GlobType[this.globSampleSize * this.globSampleSize], GlobType.NO_DATA);

        this.adapters.add(new CoastlineAdapter());
        this.adapters.add(new WaterFlattenAdapter(16));
    }

    public void addAdapter(RegionAdapter adapter) {
        this.adapters.add(adapter);
    }

    public boolean removeAdapter(RegionAdapter adapter) {
        return this.adapters.remove(adapter);
    }

    public List<RegionAdapter> getAdapters() {
        return this.adapters;
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

        EarthGenerationSettings settings = this.generationHandler.getSettings();
        Coordinate minCoordinate = pos.getMinCoordinate(settings);
        Coordinate maxCoordinate = pos.getMaxCoordinate(settings);

        OverpassTileAccess overpassTile = this.worldData.getOutlineOverpassSource().sampleArea(minCoordinate, maxCoordinate);

        GeneralOverpassSource generalOverpassSource = this.worldData.getGeneralOverpassSource();
        if (generalOverpassSource.shouldSample()) {
            overpassTile = overpassTile.merge(generalOverpassSource.sampleArea(minCoordinate, maxCoordinate));
        }

        DetailedOverpassSource detailedOverpassSource = this.worldData.getDetailedOverpassSource();
        if (detailedOverpassSource.shouldSample()) {
            overpassTile = overpassTile.merge(detailedOverpassSource.sampleArea(minCoordinate, maxCoordinate));
        }

        short[] heights = this.generateHeights(minCoordinate, maxCoordinate);
        GlobType[] globcover = this.generateGlobcover(minCoordinate, maxCoordinate);

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
        this.worldData.getHeightSource().sampleArea(this.sampledHeights, minCoordinate, maxCoordinate.add(1.0, 1.0));

        short[] resultHeights = new short[GenerationRegion.SIZE * GenerationRegion.SIZE];
        this.scaleHandler.scaleHeightRegion(resultHeights, this.sampledHeights, this.heightSampleSize, this.regionSize);

        return resultHeights;
    }

    private GlobType[] generateGlobcover(Coordinate minCoordinate, Coordinate maxCoordinate) {
        this.worldData.getGlobSource().sampleArea(this.sampledGlobs, minCoordinate, maxCoordinate);

        GlobType[] resultGlobs = ArrayUtils.defaulted(new GlobType[GenerationRegion.SIZE * GenerationRegion.SIZE], GlobType.NO_DATA);
        this.scaleHandler.scaleGlobRegion(resultGlobs, this.sampledGlobs, this.globSampleSize, this.regionSize);

        return resultGlobs;
    }

    private GenerationRegion createDefaultRegion(RegionTilePos pos) {
        short[] heights = new short[GenerationRegion.SIZE * GenerationRegion.SIZE];
        GlobType[] globcover = ArrayUtils.defaulted(new GlobType[GenerationRegion.SIZE * GenerationRegion.SIZE], GlobType.NO_DATA);
        return new GenerationRegion(pos, new RegionData(heights, globcover, new OverpassTileAccess()));
    }
}
