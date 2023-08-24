package dev.gegy.terrarium.backend.earth.climate;

import com.mojang.logging.LogUtils;
import dev.gegy.terrarium.backend.loader.Loader;
import dev.gegy.terrarium.backend.raster.RasterShape;
import org.slf4j.Logger;
import org.tukaani.xz.SingleXZInputStream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public record ClimateRasters(
        TemperatureRaster meanTemperature,
        TemperatureRaster minTemperature,
        RainfallRaster annualRainfall
) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int WIDTH = 4320;
    public static final int HEIGHT = 2160;
    private static final RasterShape SHAPE = new RasterShape(WIDTH, HEIGHT);

    public static Loader<byte[], ClimateRasters> loader(final Executor executor) {
        return bytes -> CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.of(decode(new ByteArrayInputStream(bytes)));
            } catch (final IOException e) {
                LOGGER.error("Failed to read climate rasters", e);
                return Optional.empty();
            }
        }, executor);
    }

    private static ClimateRasters decode(final InputStream input) throws IOException {
        final byte[] meanTemperature = new byte[SHAPE.size()];
        final byte[] minTemperature = new byte[SHAPE.size()];
        final byte[] annualRainfall = new byte[SHAPE.size()];

        final DataInputStream data = new DataInputStream(new SingleXZInputStream(input));
        data.readFully(meanTemperature);
        data.readFully(minTemperature);
        data.readFully(annualRainfall);

        return new ClimateRasters(
                TemperatureRaster.wrap(SHAPE, meanTemperature),
                TemperatureRaster.wrap(SHAPE, minTemperature),
                RainfallRaster.wrap(SHAPE, annualRainfall)
        );
    }
}
