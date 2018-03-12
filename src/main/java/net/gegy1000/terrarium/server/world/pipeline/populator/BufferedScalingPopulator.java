package net.gegy1000.terrarium.server.world.pipeline.populator;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;
import net.minecraft.util.math.MathHelper;

public abstract class BufferedScalingPopulator<T> implements RegionPopulator<T> {
    protected final int lowerSampleBuffer;
    protected final int upperSampleBuffer;

    private final CoordinateState coordinateState;

    public BufferedScalingPopulator(int lowerSampleBuffer, int upperSampleBuffer, CoordinateState coordinateState) {
        this.lowerSampleBuffer = lowerSampleBuffer;
        this.upperSampleBuffer = upperSampleBuffer;

        this.coordinateState = coordinateState;
    }

    @Override
    public final T populate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        double blockSizeX = regionSize.getBlockX();
        double blockSizeZ = regionSize.getBlockZ();

        double scaleFactorX = this.coordinateState.getX(blockSizeX, blockSizeZ) / blockSizeX;
        double scaleFactorZ = this.coordinateState.getZ(blockSizeX, blockSizeZ) / blockSizeZ;

        Coordinate minRegionCoordinateBlock = pos.getMinBufferedCoordinate().to(this.coordinateState);
        Coordinate maxRegionCoordinateBlock = pos.getMaxBufferedCoordinate().to(this.coordinateState);

        Coordinate minRegionCoordinate = Coordinate.min(minRegionCoordinateBlock, maxRegionCoordinateBlock);
        Coordinate maxRegionCoordinate = Coordinate.max(minRegionCoordinateBlock, maxRegionCoordinateBlock);

        int minSampleX = MathHelper.floor(minRegionCoordinate.getX()) - this.lowerSampleBuffer;
        int minSampleZ = MathHelper.floor(minRegionCoordinate.getZ()) - this.lowerSampleBuffer;

        int maxSampleX = MathHelper.ceil(maxRegionCoordinate.getX()) + this.upperSampleBuffer;
        int maxSampleZ = MathHelper.ceil(maxRegionCoordinate.getZ()) + this.upperSampleBuffer;

        int sampleWidth = maxSampleX - minSampleX;
        int sampleHeight = maxSampleZ - minSampleZ;

        double originOffsetX = minRegionCoordinate.getX() - minSampleX;
        double originOffsetZ = minRegionCoordinate.getZ() - minSampleZ;

        return this.populate(settings, this.coordinateState, pos, minSampleX, minSampleZ, sampleWidth, sampleHeight, width, height, scaleFactorX, scaleFactorZ, originOffsetX, originOffsetZ);
    }

    protected abstract T populate(GenerationSettings settings, CoordinateState originState, RegionTilePos pos,
                                  int minSampleX, int minSampleZ, int sampleWidth, int sampleHeight, int width, int height,
                                  double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ
    );
}
