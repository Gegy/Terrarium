package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class ClimateSampler {
    private final WorldClimateRaster source;

    public ClimateSampler(WorldClimateRaster source) {
        this.source = source;
    }

    public DataOp<ShortRaster> annualRainfall() {
        return DataOp.of(view -> {
            ShortRaster annualRainfall = ShortRaster.create(view);
            for (int y = 0; y < view.getHeight(); y++) {
                for (int x = 0; x < view.getWidth(); x++) {
                    annualRainfall.set(x, y, this.source.getAnnualRainfall(view.getX() + x, view.getY() + y));
                }
            }
            return CompletableFuture.completedFuture(Optional.of(annualRainfall));
        });
    }

    // TODO: Can probably make a byte raster
    public DataOp<FloatRaster> meanTemperature() {
        return DataOp.of(view -> {
            FloatRaster temperatureRaster = FloatRaster.create(view);
            for (int y = 0; y < view.getHeight(); y++) {
                for (int x = 0; x < view.getWidth(); x++) {
                    temperatureRaster.set(x, y, this.source.getMeanTemperature(view.getX() + x, view.getY() + y));
                }
            }
            return CompletableFuture.completedFuture(Optional.of(temperatureRaster));
        });
    }

    public DataOp<FloatRaster> minTemperature() {
        return DataOp.of(view -> {
            FloatRaster temperatureRaster = FloatRaster.create(view);
            for (int y = 0; y < view.getHeight(); y++) {
                for (int x = 0; x < view.getWidth(); x++) {
                    temperatureRaster.set(x, y, this.source.getMinTemperature(view.getX() + x, view.getY() + y));
                }
            }
            return CompletableFuture.completedFuture(Optional.of(temperatureRaster));
        });
    }
}
