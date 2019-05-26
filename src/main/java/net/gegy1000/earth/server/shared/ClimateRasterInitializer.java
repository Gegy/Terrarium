package net.gegy1000.earth.server.shared;

import net.gegy1000.earth.server.util.ProcessTracker;
import net.gegy1000.earth.server.util.ProgressTracker;
import net.gegy1000.earth.server.util.TrackedInputStream;
import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.io.InputStream;

public final class ClimateRasterInitializer implements SharedDataInitializer {
    private static final String JANUARY_PATH = "/data/earth/january_climate.xz";
    private static final String JANUARY_DESC = "initializer.terrarium.climate_rasters.january";

    private static final String JULY_PATH = "/data/earth/july_climate.xz";
    private static final String JULY_DESC = "initializer.terrarium.climate_rasters.july";

    @Override
    public void initialize(SharedEarthData data, ProcessTracker processTracker) {
        ProgressTracker master = processTracker.push(I18n.format("initializer.terrarium.climate_rasters"), 2);

        master.use(() -> {
            WorldClimateRaster january = this.loadRaster(JANUARY_PATH, I18n.format(JANUARY_DESC), processTracker);
            data.put(SharedEarthData.JANUARY_CLIMATE, january);

            master.step(1);

            WorldClimateRaster july = this.loadRaster(JULY_PATH, I18n.format(JULY_DESC), processTracker);
            data.put(SharedEarthData.JULY_CLIMATE, july);

            master.step(1);
        });
    }

    private WorldClimateRaster loadRaster(String path, String description, ProcessTracker processTracker) throws IOException {
        try (
                InputStream input = new TrackedInputStream(WorldClimateRaster.class.getResourceAsStream(path))
                        .submitTo(description, processTracker)
        ) {
            return WorldClimateRaster.parse(input);
        }
    }
}
