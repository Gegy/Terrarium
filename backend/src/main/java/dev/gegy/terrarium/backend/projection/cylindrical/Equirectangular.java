package dev.gegy.terrarium.backend.projection.cylindrical;

import dev.gegy.terrarium.backend.earth.EarthConstants;

public class Equirectangular implements CylindricalProjection {
    private final int blocksX;
    private final int blocksZ;

    public Equirectangular(final double metersPerBlock) {
        blocksX = (int) Math.floor(EarthConstants.CIRCUMFERENCE_EQUATOR / metersPerBlock);
        blocksZ = blocksX / 2;
    }

    @Override
    public double blockX(final double lon) {
        return lon / 360.0 * blocksX;
    }

    @Override
    public double blockZ(final double lat) {
        return -lat / 180.0 * blocksZ;
    }

    @Override
    public double lon(final double blockX) {
        return blockX / blocksX * 360.0;
    }

    @Override
    public double lat(final double blockZ) {
        return -blockZ / blocksZ * 180.0;
    }
}
