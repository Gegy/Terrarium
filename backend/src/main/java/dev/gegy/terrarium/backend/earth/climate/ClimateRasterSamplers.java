package dev.gegy.terrarium.backend.earth.climate;

import com.google.common.base.Suppliers;
import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.layer.LeveledRasterSampler;
import dev.gegy.terrarium.backend.layer.RasterSampler;
import dev.gegy.terrarium.backend.loader.Loader;
import dev.gegy.terrarium.backend.raster.Raster;
import dev.gegy.terrarium.backend.raster.RasterShape;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public record ClimateRasterSamplers(
        LeveledRasterSampler<TemperatureRaster> meanTemperature,
        LeveledRasterSampler<TemperatureRaster> minTemperature,
        LeveledRasterSampler<RainfallRaster> annualRainfall
) {
    public static ClimateRasterSamplers create(final Loader<Void, ClimateRasters> loader, final Executor executor) {
        final Supplier<CompletableFuture<Optional<ClimateRasters>>> future = Suppliers.memoize(() -> loader.load(null));
        return new ClimateRasterSamplers(
                createSampler(ClimateRasters::meanTemperature, TemperatureRaster::create, future, executor),
                createSampler(ClimateRasters::minTemperature, TemperatureRaster::create, future, executor),
                createSampler(ClimateRasters::annualRainfall, RainfallRaster::create, future, executor)
        );
    }

    private static <V extends Raster> LeveledRasterSampler<V> createSampler(final Function<ClimateRasters, V> rasterGetter, final Function<RasterShape, V> rasterFactory, final Supplier<CompletableFuture<Optional<ClimateRasters>>> future, final Executor executor) {
        return new LeveledRasterSampler<>(new RasterSampler<>() {
            @Override
            public CompletableFuture<Optional<V>> get(final GeoView view) {
                return future.get().thenApplyAsync(
                        opt -> opt.map(climateRasters -> {
                            final V source = rasterGetter.apply(climateRasters);
                            final V target = rasterFactory.apply(view.shape());
                            target.copyFromClipped(source, -view.x0(), -view.z0());
                            return target;
                        }),
                        executor
                );
            }

            @Override
            public int width() {
                return ClimateRasters.WIDTH;
            }

            @Override
            public int height() {
                return ClimateRasters.HEIGHT;
            }
        });
    }
}
