package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;

import java.util.concurrent.CompletableFuture;

public final class ClimateSampler {
    private final WorldClimateRaster source;

    public ClimateSampler(WorldClimateRaster source) {
        this.source = source;
    }

    public DataOp<ShortRaster> monthlyRainfall() {
        return DataOp.of(view -> {
            ShortRaster rainfallRaster = ShortRaster.create(view);
            for (int y = 0; y < view.getHeight(); y++) {
                for (int x = 0; x < view.getWidth(); x++) {
                    rainfallRaster.set(x, y, this.source.getMonthlyRainfall(view.getX() + x, view.getY() + y));
                }
            }

            return CompletableFuture.completedFuture(rainfallRaster);
        });
    }

    // TODO: Can probably make a byte raster
    public DataOp<FloatRaster> averageTemperature() {
        return DataOp.of(view -> {
            FloatRaster temperatureRaster = FloatRaster.create(view);
            for (int y = 0; y < view.getHeight(); y++) {
                for (int x = 0; x < view.getWidth(); x++) {
                    temperatureRaster.set(x, y, this.source.getAverageTemperature(view.getX() + x, view.getY() + y));
                }
            }

            return CompletableFuture.completedFuture(temperatureRaster);
        });
    }
}
