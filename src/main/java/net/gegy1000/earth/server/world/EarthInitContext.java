package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.util.ZoomLevels;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.data.source.ElevationSource;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.world.World;

import static net.gegy1000.earth.server.world.EarthWorldType.*;

public final class EarthInitContext {
    public final World world;
    public final GenerationSettings settings;

    public final CoordinateReference lngLatCrs;
    public final Zoomable<CoordinateReference> elevationRasterCrs;
    public final CoordinateReference landcoverRasterCrs;
    public final CoordinateReference climateRasterCrs;

    private EarthInitContext(World world, GenerationSettings settings) {
        this.world = world;
        this.settings = settings;

        double worldScale = settings.getDouble(WORLD_SCALE);

        double metersPerDegree = EarthWorld.EQUATOR_CIRCUMFERENCE / 360.0;
        this.lngLatCrs = CoordinateReference.lngLat(metersPerDegree / worldScale);

        this.elevationRasterCrs = Zoomable.create(ZoomLevels.range(0, 3), zoom -> ElevationSource.crs(worldScale, zoom));

        this.landcoverRasterCrs = CoordinateReference.scale(LANDCOVER_SCALE / worldScale);
        this.climateRasterCrs = CoordinateReference.scale(CLIMATE_SCALE / worldScale);
    }

    public static EarthInitContext from(World world, GenerationSettings settings) {
        return new EarthInitContext(world, settings);
    }
}
