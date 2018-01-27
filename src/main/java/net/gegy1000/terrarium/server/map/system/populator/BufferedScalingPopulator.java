package net.gegy1000.terrarium.server.map.system.populator;

import net.gegy1000.terrarium.server.map.RegionTilePos;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

import java.util.function.Function;

public abstract class BufferedScalingPopulator<T> implements RegionPopulator<T> {
    private final int lowerSampleBuffer;
    private final int upperSampleBuffer;

    private final Function<Coordinate, Double> sampleXCoordinate;
    private final Function<Coordinate, Double> sampleZCoordinate;

    public BufferedScalingPopulator(int lowerSampleBuffer, int upperSampleBuffer, Function<Coordinate, Double> sampleXCoordinate, Function<Coordinate, Double> sampleZCoordinate) {
        this.lowerSampleBuffer = lowerSampleBuffer;
        this.upperSampleBuffer = upperSampleBuffer;

        this.sampleXCoordinate = sampleXCoordinate;
        this.sampleZCoordinate = sampleZCoordinate;
    }

    @Override
    public final T populate(EarthGenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        double scaleFactorX = this.getScaleFactorX(regionSize);
        double scaleFactorZ = this.getScaleFactorZ(regionSize);

        Coordinate minRegionCoordinate = pos.getMinBufferedCoordinate(settings);
        Coordinate maxRegionCoordinate = pos.getMaxBufferedCoordinate(settings);

        int minSampleX = MathHelper.floor(this.sampleXCoordinate.apply(minRegionCoordinate)) - this.lowerSampleBuffer;
        int minSampleZ = MathHelper.floor(this.sampleZCoordinate.apply(minRegionCoordinate)) - this.lowerSampleBuffer;

        int maxSampleX = MathHelper.ceil(this.sampleXCoordinate.apply(maxRegionCoordinate)) + this.upperSampleBuffer;
        int maxSampleZ = MathHelper.ceil(this.sampleZCoordinate.apply(maxRegionCoordinate)) + this.upperSampleBuffer;

        int sampleWidth = maxSampleX - minSampleX;
        int sampleHeight = maxSampleZ - minSampleZ;

        double originOffsetX = this.sampleXCoordinate.apply(minRegionCoordinate) - minSampleX;
        double originOffsetZ = this.sampleZCoordinate.apply(minRegionCoordinate) - minSampleZ;

        return this.populate(settings, pos, minSampleX, minSampleZ, sampleWidth, sampleHeight, width, height, scaleFactorX, scaleFactorZ, originOffsetX, originOffsetZ);
    }

    protected abstract double getScaleFactorX(Coordinate regionSize);

    protected abstract double getScaleFactorZ(Coordinate regionSize);

    protected abstract T populate(EarthGenerationSettings settings, RegionTilePos pos, int minSampleX, int minSampleZ,
                                  int sampleWidth, int sampleHeight, int width, int height,
                                  double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ
    );
}
