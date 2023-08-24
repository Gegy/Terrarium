package dev.gegy.terrarium.backend.projection.cylindrical;

import dev.gegy.terrarium.backend.earth.EarthConstants;

public class Mercator implements CylindricalProjection {
    private final int blocksX;

    public Mercator(final double metersPerBlock) {
        blocksX = (int) Math.floor(EarthConstants.CIRCUMFERENCE_EQUATOR / metersPerBlock);
    }

    @Override
    public double blockX(final double lon) {
        return lon / 360.0 * blocksX;
    }

    @Override
    public double blockZ(final double lat) {
        return projectY(Math.toRadians(lat)) / (2.0 * Math.PI) * blocksX;
    }

    @Override
    public double lon(final double blockX) {
        return blockX / blocksX * 360.0;
    }

    @Override
    public double lat(final double blockZ) {
        return Math.toDegrees(unprojectY(blockZ / blocksX * (2.0 * Math.PI)));
    }

    private static double projectY(final double lat) {
        return Math.log(Math.tan(Math.PI / 4.0 - lat / 2.0));
    }

    private static double unprojectY(final double y) {
        return (Math.PI / 4.0 - Math.atan(Math.exp(y))) * 2.0;
    }
}
