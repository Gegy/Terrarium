package net.gegy1000.terrarium.server.map;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.map.adapter.CoastlineAdapter;
import net.gegy1000.terrarium.server.map.adapter.WaterFlattenAdapter;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;
import net.gegy1000.terrarium.server.map.system.RegionPopulationSystem;
import net.gegy1000.terrarium.server.map.system.component.TerrariumComponentTypes;
import net.gegy1000.terrarium.server.map.system.populator.CoverRegionPopulator;
import net.gegy1000.terrarium.server.map.system.populator.HeightRegionPopulator;
import net.gegy1000.terrarium.server.map.system.populator.OverpassRegionPopulator;
import net.gegy1000.terrarium.server.map.system.populator.SlopeRegionPopulator;
import net.gegy1000.terrarium.server.map.system.sampler.DataSampler;
import net.gegy1000.terrarium.server.map.system.sampler.GlobSampler;
import net.gegy1000.terrarium.server.map.system.sampler.HeightSampler;
import net.gegy1000.terrarium.server.map.system.sampler.OverpassSampler;
import net.gegy1000.terrarium.server.map.system.sampler.SlopeSampler;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GenerationRegionHandler {
    private final EarthGenerationSettings settings;
    private final TerrariumWorldData worldData;

    private final Coordinate bufferedRegionSize;

    private final LoadingCache<RegionTilePos, GenerationRegion> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .maximumSize(8)
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

    private final RegionPopulationSystem populationSystem;

    public GenerationRegionHandler(World world, EarthGenerationSettings settings, TerrariumWorldData worldData) {
        this.settings = settings;
        this.worldData = worldData;

        this.bufferedRegionSize = Coordinate.fromBlock(settings, GenerationRegion.BUFFERED_SIZE, GenerationRegion.BUFFERED_SIZE);

        HeightSampler heightSampler = new HeightSampler(worldData.getHeightSource());
        SlopeSampler slopeSampler = new SlopeSampler(heightSampler);
        GlobSampler coverSampler = new GlobSampler(worldData.getGlobSource());
        List<DataSampler<OverpassTileAccess>> overpassSamplers = Lists.newArrayList(
                new OverpassSampler(worldData.getOutlineOverpassSource()),
                new OverpassSampler(worldData.getGeneralOverpassSource()),
                new OverpassSampler(worldData.getDetailedOverpassSource())
        );

        Interpolation.Method interpolationMethod = Interpolation.Method.CUBIC;
        double scale = 1.0 / settings.worldScale;
        if (scale >= 45.0) {
            interpolationMethod = Interpolation.Method.LINEAR;
        } else if (scale >= 20.0) {
            interpolationMethod = Interpolation.Method.COSINE;
        }

        Random random = new Random(world.getSeed());
        this.populationSystem = RegionPopulationSystem.builder(settings)
                .withComponent(TerrariumComponentTypes.HEIGHT, new HeightRegionPopulator(random, heightSampler, interpolationMethod))
                .withComponent(TerrariumComponentTypes.SLOPE, new SlopeRegionPopulator(slopeSampler))
                .withComponent(TerrariumComponentTypes.COVER, new CoverRegionPopulator(coverSampler))
                .withComponent(TerrariumComponentTypes.OVERPASS, new OverpassRegionPopulator(overpassSamplers))
                .withAdapter(new CoastlineAdapter())
                .withAdapter(new WaterFlattenAdapter(16))
                .build();
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
        Coordinate minCoordinate = pos.getMinBufferedCoordinate(this.settings);
        Coordinate maxCoordinate = pos.getMaxBufferedCoordinate(this.settings);

        if (!minCoordinate.inWorldBounds() || !maxCoordinate.inWorldBounds()) {
            return this.createDefaultRegion(pos);
        }

        RegionData data = this.populationSystem.populateData(pos, this.bufferedRegionSize, GenerationRegion.BUFFERED_SIZE, GenerationRegion.BUFFERED_SIZE);
        return new GenerationRegion(pos, data);
    }

    private GenerationRegion createDefaultRegion(RegionTilePos pos) {
        return new GenerationRegion(pos, new RegionData(Collections.emptyMap()));
    }
}
