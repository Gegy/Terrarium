package net.gegy1000.earth.server.shared;

import net.gegy1000.earth.server.util.ProcessTracker;
import net.gegy1000.earth.server.util.ProgressTracker;
import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.gegy1000.earth.server.world.data.source.cache.CachingInput;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClimateRasterInitializer implements SharedDataInitializer {
    private static final Path PATH = TiledDataSource.GLOBAL_CACHE_ROOT.resolve("climatic_variables.xz");
    private static final String URL = "https://terrariumearth.azureedge.net/geo2/climatic_variables.xz";

    @Override
    public void initialize(SharedEarthData data, ProcessTracker processTracker) {
        ProgressTracker master = processTracker.push(new TextComponentTranslation("initializer.terrarium.climate_raster"), 1);

        master.use(() -> {
            try (InputStream input = getStream()) {
                data.put(SharedEarthData.CLIMATIC_VARIABLES, WorldClimateRaster.parse(input));
            }
            master.step(1);
        });
    }

    private static InputStream getStream() throws IOException {
        if (Files.exists(PATH)) {
            return Files.newInputStream(PATH);
        }

        return CachingInput.getCachingStream(getRemoteStream(), () -> Files.newOutputStream(PATH), e -> {
            try {
                Files.delete(PATH);
            } catch (IOException ignored) {
            }
        });
    }

    private static InputStream getRemoteStream() throws IOException {
        URLConnection connection = new URL(URL).openConnection();
        return connection.getInputStream();
    }
}
