package net.gegy1000.terrarium.server.world.pipeline.populator;

import net.gegy1000.terrarium.server.util.Voronoi;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.sampler.DataSampler;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;

public class ScaledCoverRegionPopulator extends BufferedScalingPopulator<CoverRasterTile> {
    private final DataSampler<CoverType[]> sampler;
    private final Voronoi voronoi;

    public ScaledCoverRegionPopulator(DataSampler<CoverType[]> sampler, CoordinateState coordinateState) {
        super(1, 1, coordinateState);
        this.sampler = sampler;

        this.voronoi = new Voronoi(Voronoi.DistanceFunc.EUCLIDEAN, 0.9, 4, 1000);
    }

    @Override
    protected CoverRasterTile populate(GenerationSettings settings, int minSampleX, int minSampleZ, int sampleWidth, int sampleHeight, int width, int height, double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ) {
        CoverType[] sampledCover = this.sampler.sample(settings, minSampleX, minSampleZ, sampleWidth + 1, sampleHeight + 1);

        CoverType[] scaledCover = new CoverType[width * height];
        this.voronoi.scale(sampledCover, scaledCover, minSampleX, minSampleZ, sampleWidth + 1, sampleHeight + 1, width, height, scaleFactorX, scaleFactorZ, originOffsetX, originOffsetZ);

        return new CoverRasterTile(scaledCover, width, height);
    }

    @Override
    public Class<CoverRasterTile> getType() {
        return CoverRasterTile.class;
    }
}
