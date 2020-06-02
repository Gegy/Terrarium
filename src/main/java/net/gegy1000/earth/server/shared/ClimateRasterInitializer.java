package net.gegy1000.earth.server.shared;

import net.gegy1000.earth.server.util.ProcessTracker;
import net.gegy1000.earth.server.util.TrackedInputStream;
import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.gegy1000.earth.server.world.data.source.cache.CachingInput;
import net.gegy1000.terrarium.server.world.data.source.TerrariumCacheDirs;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClimateRasterInitializer implements SharedDataInitializer {
    private static final Path PATH = TerrariumCacheDirs.GLOBAL_ROOT.resolve("climatic_variables.xz");
    private static final String URL = "https://terrariumearth.azureedge.net/geo3/climatic_variables.xz";

    @Override
    public void initialize(SharedEarthData data, ProcessTracker processTracker) {
        try (TrackedInputStream input = new TrackedInputStream(getStream())) {
            input.submitTo(new TextComponentTranslation("initializer.terrarium.climate_raster"), processTracker);
            data.put(SharedEarthData.CLIMATIC_VARIABLES, WorldClimateRaster.parse(input));
        } catch (IOException e) {
            processTracker.raiseException(e);
        }
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
        connection.setConnectTimeout(10 * 1000);
        connection.setReadTimeout(30 * 1000);
        connection.setRequestProperty(HttpHeaders.USER_AGENT, "terrarium");

        return connection.getInputStream();
    }
}
