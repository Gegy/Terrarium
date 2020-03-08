package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.data.source.ElevationSource;
import net.gegy1000.earth.server.world.data.source.SoilSource;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;

import static net.gegy1000.earth.server.world.EarthWorldType.*;

public final class EarthInitContext {
    public final GenerationSettings settings;

    public final CoordinateReference lngLatCrs;
    public final Zoomable<CoordinateReference> elevationRasterCrs;
    public final CoordinateReference landcoverRasterCrs;
    public final CoordinateReference climateRasterCrs;
    public final Zoomable<CoordinateReference> soilRasterCrs;

    private EarthInitContext(GenerationSettings settings) {
        this.settings = settings;

        double worldScale = settings.getDouble(WORLD_SCALE);

        double metersPerDegree = EarthWorld.EQUATOR_CIRCUMFERENCE / 360.0;
        this.lngLatCrs = CoordinateReference.lngLat(metersPerDegree / worldScale);

        this.elevationRasterCrs = Zoomable.create(ElevationSource.zoomLevels(), zoom -> ElevationSource.crs(worldScale, zoom));

        this.landcoverRasterCrs = CoordinateReference.scale(LANDCOVER_SCALE / worldScale);
        this.climateRasterCrs = CoordinateReference.scale(CLIMATE_SCALE / worldScale);

        this.soilRasterCrs = Zoomable.create(SoilSource.zoomLevels(), zoom -> SoilSource.crs(worldScale, zoom));
    }

    public static EarthInitContext from(GenerationSettings settings) {
        return new EarthInitContext(settings);
    }
}
