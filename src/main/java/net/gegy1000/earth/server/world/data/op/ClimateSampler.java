package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;

public final class ClimateSampler {
    private final WorldClimateRaster source;

    public ClimateSampler(WorldClimateRaster source) {
        this.source = source;
    }

    public DataOp<ShortRaster> annualRainfall() {
        return DataOp.ofLazy(view -> {
            ShortRaster annualRainfall = ShortRaster.create(view);
            for (int y = 0; y < view.height(); y++) {
                for (int x = 0; x < view.width(); x++) {
                    annualRainfall.set(x, y, this.source.getAnnualRainfall(view.minX() + x, view.minY() + y));
                }
            }
            return annualRainfall;
        });
    }

    // TODO: Can probably make a byte raster
    public DataOp<FloatRaster> meanTemperature() {
        return DataOp.ofLazy(view -> {
            FloatRaster temperatureRaster = FloatRaster.create(view);
            for (int y = 0; y < view.height(); y++) {
                for (int x = 0; x < view.width(); x++) {
                    temperatureRaster.set(x, y, this.source.getMeanTemperature(view.minX() + x, view.minY() + y));
                }
            }
            return temperatureRaster;
        });
    }

    public DataOp<FloatRaster> minTemperature() {
        return DataOp.ofLazy(view -> {
            FloatRaster temperatureRaster = FloatRaster.create(view);
            for (int y = 0; y < view.height(); y++) {
                for (int x = 0; x < view.width(); x++) {
                    temperatureRaster.set(x, y, this.source.getMinTemperature(view.minX() + x, view.minY() + y));
                }
            }
            return temperatureRaster;
        });
    }
}
