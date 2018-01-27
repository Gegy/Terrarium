package net.gegy1000.terrarium.server.map.system.populator;

import net.gegy1000.terrarium.server.map.RegionTilePos;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.source.glob.CoverTileAccess;
import net.gegy1000.terrarium.server.map.system.sampler.DataSampler;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.Voronoi;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class CoverRegionPopulator extends BufferedScalingPopulator<CoverTileAccess> {
    private static final int LOWER_SAMPLE_BUFFER = 1;
    private static final int UPPER_SAMPLE_BUFFER = 1;

    private final DataSampler<CoverType[]> coverSampler;
    private final Voronoi voronoi;

    public CoverRegionPopulator(DataSampler<CoverType[]> coverSampler) {
        super(LOWER_SAMPLE_BUFFER, UPPER_SAMPLE_BUFFER, Coordinate::getGlobX, Coordinate::getGlobZ);
        this.coverSampler = coverSampler;

        this.voronoi = new Voronoi(Voronoi.DistanceFunc.EUCLIDEAN, 0.9, 4, 1000);
    }

    @Override
    protected CoverTileAccess populate(EarthGenerationSettings settings, RegionTilePos pos, int minSampleX, int minSampleZ, int sampleWidth, int sampleHeight, int width, int height, double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ) {
        CoverType[] sampledCover = this.coverSampler.sample(settings, minSampleX, minSampleZ, sampleWidth + 1, sampleHeight + 1);

        CoverType[] scaledCover = new CoverType[width * height];
        this.voronoi.scale(sampledCover, scaledCover, minSampleX, minSampleZ, sampleWidth + 1, sampleHeight + 1, width, height, scaleFactorX, scaleFactorZ, originOffsetX, originOffsetZ);

        return new CoverTileAccess(scaledCover, width, height);
    }

    @Override
    protected double getScaleFactorX(Coordinate regionSize) {
        return regionSize.getGlobX() / regionSize.getBlockX();
    }

    @Override
    protected double getScaleFactorZ(Coordinate regionSize) {
        return regionSize.getGlobZ() / regionSize.getBlockZ();
    }
}
