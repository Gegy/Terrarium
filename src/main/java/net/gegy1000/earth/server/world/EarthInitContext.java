package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.util.zoom.ForZoom;
import net.gegy1000.earth.server.world.data.source.StdSource;
import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;

import static net.gegy1000.earth.server.world.EarthWorldType.WORLD_SCALE;

public final class EarthInitContext {
    public final GenerationSettings settings;

    public final CoordinateReference lngLatCrs;
    public final CoordinateReference climateRasterCrs;

    public final ForZoom<CoordinateReference> stdRasterCrs;

    private EarthInitContext(GenerationSettings settings) {
        this.settings = settings;

        double worldScale = this.settings.getDouble(WORLD_SCALE);

        double metersPerDegree = EarthWorld.EQUATOR_CIRCUMFERENCE / 360.0;
        this.lngLatCrs = CoordinateReference.lngLat(metersPerDegree / worldScale);

        this.climateRasterCrs = WorldClimateRaster.crs(worldScale);

        this.stdRasterCrs = zoom -> StdSource.crs(worldScale, zoom);
    }

    public static EarthInitContext from(GenerationSettings settings) {
        return new EarthInitContext(settings);
    }
}
