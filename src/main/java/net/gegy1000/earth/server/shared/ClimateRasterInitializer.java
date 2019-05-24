package net.gegy1000.earth.server.shared;

import net.gegy1000.earth.server.util.OpProgressWatcher;
import net.gegy1000.earth.server.util.WatchedInputStream;
import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.io.InputStream;

public final class ClimateRasterInitializer implements SharedDataInitializer {
    @Override
    public void initialize(SharedEarthData data, OpProgressWatcher progress) throws SharedInitException {
        try (
                InputStream januaryInput = WorldClimateRaster.class.getResourceAsStream("/data/earth/january_climate.xz");
                InputStream julyInput = WorldClimateRaster.class.getResourceAsStream("/data/earth/july_climate.xz")
        ) {
            WorldClimateRaster januaryClimate = WorldClimateRaster.parse(new WatchedInputStream(januaryInput, progress.map(p -> p / 2.0)));
            data.put(SharedEarthData.JANUARY_CLIMATE, januaryClimate);

            WorldClimateRaster julyClimate = WorldClimateRaster.parse(new WatchedInputStream(julyInput, progress.map(p -> 0.5 + p / 2.0)));
            data.put(SharedEarthData.JULY_CLIMATE, julyClimate);
        } catch (IOException e) {
            throw new SharedInitException(e);
        }
    }

    @Override
    public String getDescription() {
        return I18n.format("initializer.terrarium.climate_rasters");
    }
}
