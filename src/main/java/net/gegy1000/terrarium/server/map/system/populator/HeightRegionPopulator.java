package net.gegy1000.terrarium.server.map.system.populator;

import net.gegy1000.terrarium.server.map.RegionTilePos;
import net.gegy1000.terrarium.server.map.source.height.HeightTileAccess;
import net.gegy1000.terrarium.server.map.system.sampler.DataSampler;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

public class HeightRegionPopulator extends BufferedScalingPopulator<HeightTileAccess> {
    private static final int LOWER_SAMPLE_BUFFER = 0;
    private static final int UPPER_SAMPLE_BUFFER = 1;

    private final DataSampler<short[]> heightSampler;

    public HeightRegionPopulator(DataSampler<short[]> heightSampler) {
        super(LOWER_SAMPLE_BUFFER, UPPER_SAMPLE_BUFFER, Coordinate::getGlobalX, Coordinate::getGlobalZ);
        this.heightSampler = heightSampler;
    }

    @Override
    protected HeightTileAccess populate(EarthGenerationSettings settings, RegionTilePos pos, int minSampleX, int minSampleZ,
                                        int sampleWidth, int sampleHeight, int width, int height,
                                        double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ
    ) {
        double terrainHeightScale = settings.terrainHeightScale * settings.worldScale;

        short[] sampledHeights = this.heightSampler.sample(settings, minSampleX, minSampleZ, sampleWidth, sampleHeight);
        short[] resultHeights = this.scaleHeightRegion(sampledHeights, sampleWidth, width, height, scaleFactorX, scaleFactorZ, originOffsetX, originOffsetZ, terrainHeightScale);

        return new HeightTileAccess(resultHeights, width, height);
    }

    @Override
    protected double getScaleFactorX(Coordinate regionSize) {
        return regionSize.getGlobalX() / regionSize.getBlockX();
    }

    @Override
    protected double getScaleFactorZ(Coordinate regionSize) {
        return regionSize.getGlobalZ() / regionSize.getBlockZ();
    }

    private short[] scaleHeightRegion(short[] sampledHeights, int sampleWidth, int width, int height, double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ, double terrainHeightScale) {
        short[] resultHeights = new short[width * height];

        for (int scaledZ = 0; scaledZ < height; scaledZ++) {
            double sampleZ = scaledZ * scaleFactorZ + originOffsetZ + LOWER_SAMPLE_BUFFER;
            int originZ = MathHelper.floor(sampleZ);
            double intermediateZ = sampleZ - originZ;

            for (int scaledX = 0; scaledX < width; scaledX++) {
                double sampleX = scaledX * scaleFactorX + originOffsetX + LOWER_SAMPLE_BUFFER;
                int originX = MathHelper.floor(sampleX);
                double intermediateX = sampleX - originX;

                short heightValue = this.interpolatePoint(sampledHeights, sampleWidth, originX, originZ, intermediateX, intermediateZ, terrainHeightScale);
                resultHeights[scaledX + scaledZ * width] = heightValue;
            }
        }

        return resultHeights;
    }

    private short interpolatePoint(short[] sampledHeights, int sampleWidth, int originX, int originZ, double intermediateX, double intermediateZ, double terrainHeightScale) {
        int sampleIndex = originX + originZ * sampleWidth;

        double current = sampledHeights[sampleIndex];
        double south = sampledHeights[sampleIndex + sampleWidth];
        double east = sampledHeights[sampleIndex + 1];
        double southEast = sampledHeights[sampleIndex + sampleWidth + 1];

        double y1 = Interpolation.cosine(current, south, intermediateZ);
        double y2 = Interpolation.cosine(east, southEast, intermediateZ);

        double interpolatedHeight = Interpolation.cosine(y1, y2, intermediateX);
        short scaled = (short) (interpolatedHeight * terrainHeightScale);

        if (interpolatedHeight >= 0.0 && scaled < 1) {
            return 1;
        } else {
            return scaled;
        }
    }
}
