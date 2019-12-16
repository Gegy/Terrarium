package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.util.Zoomed;
import net.gegy1000.earth.server.world.data.source.ElevationSource;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.coordinate.LatLngCoordRef;
import net.gegy1000.terrarium.server.world.coordinate.LngLatCoordRef;
import net.gegy1000.terrarium.server.world.coordinate.ScaledCoordRef;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.world.World;

import java.util.stream.IntStream;

import static net.gegy1000.earth.server.world.EarthWorldType.*;

public final class EarthInitContext {
    public final World world;
    public final GenerationSettings settings;

    public final double worldScale;

    public final CoordinateReference latLngCrs;
    public final CoordinateReference lngLatCrs;
    public final Zoomed<CoordinateReference> elevationRasterCrs;
    public final CoordinateReference landcoverRasterCrs;
    public final CoordinateReference climateRasterCrs;

    private EarthInitContext(World world, GenerationSettings settings) {
        this.world = world;
        this.settings = settings;

        this.worldScale = 1.0 / settings.getDouble(WORLD_SCALE);

        double metersPerDegree = EarthWorld.EQUATOR_CIRCUMFERENCE / 360.0;
        this.latLngCrs = new LatLngCoordRef(this.worldScale * metersPerDegree);
        this.lngLatCrs = new LngLatCoordRef(this.worldScale * metersPerDegree);

        this.elevationRasterCrs = Zoomed.create(IntStream.of(0, 1, 2), zoom -> ElevationSource.crs(this.worldScale, zoom));

        this.landcoverRasterCrs = new ScaledCoordRef(this.worldScale * LANDCOVER_SCALE);
        this.climateRasterCrs = new ScaledCoordRef(this.worldScale * CLIMATE_SCALE);
    }

    public static EarthInitContext from(World world, GenerationSettings settings) {
        return new EarthInitContext(world, settings);
    }
}
