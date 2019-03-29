package net.gegy1000.earth.server.world.pipeline.data;

import net.gegy1000.earth.server.world.pipeline.source.WorldClimateDataset;
import net.gegy1000.terrarium.server.world.pipeline.data.DataFuture;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;

import java.util.concurrent.CompletableFuture;

public final class ClimateSampler {
    private final WorldClimateDataset source;

    public ClimateSampler(WorldClimateDataset source) {
        this.source = source;
    }

    public DataFuture<ShortRaster> annualRainfall() {
        return DataFuture.of((engine, view) -> {
            int width = view.getWidth();
            int height = view.getHeight();

            short[] buffer = new short[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    buffer[x + y * width] = this.source.getAnnualRainfall(view.getX() + x, view.getY() + y);
                }
            }

            return CompletableFuture.completedFuture(new ShortRaster(buffer, width, height));
        });
    }

    public DataFuture<FloatRaster> averageTemperature() {
        return DataFuture.of((engine, view) -> {
            int width = view.getWidth();
            int height = view.getHeight();

            float[] buffer = new float[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    buffer[x + y * width] = this.source.getAverageTemperature(view.getX() + x, view.getY() + y);
                }
            }

            return CompletableFuture.completedFuture(new FloatRaster(buffer, width, height));
        });
    }
}
