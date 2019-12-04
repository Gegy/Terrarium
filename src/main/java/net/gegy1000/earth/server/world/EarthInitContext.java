package net.gegy1000.earth.server.world;

import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.coordinate.LatLngCoordRef;
import net.gegy1000.terrarium.server.world.coordinate.LngLatCoordRef;
import net.gegy1000.terrarium.server.world.coordinate.ScaledCoordRef;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.world.World;

import static net.gegy1000.earth.server.world.EarthWorldType.*;

public final class EarthInitContext {
    public final World world;
    public final GenerationSettings settings;

    public final double worldScale;

    public final CoordinateReference latLngCoordinates;
    public final CoordinateReference lngLatCoordinates;
    public final CoordinateReference srtmRaster;
    public final CoordinateReference landcoverRaster;
    public final CoordinateReference climateRaster;

    private EarthInitContext(World world, GenerationSettings settings) {
        this.world = world;
        this.settings = settings;

        this.worldScale = 1.0 / settings.getDouble(WORLD_SCALE);
        this.latLngCoordinates = new LatLngCoordRef(this.worldScale * SRTM_SCALE * 1200.0);
        this.lngLatCoordinates = new LngLatCoordRef(this.worldScale * SRTM_SCALE * 1200.0);
        this.srtmRaster = new ScaledCoordRef(this.worldScale * SRTM_SCALE);
        this.landcoverRaster = new ScaledCoordRef(this.worldScale * LANDCOVER_SCALE);
        this.climateRaster = new ScaledCoordRef(this.worldScale * CLIMATE_SCALE);
    }

    public static EarthInitContext from(World world, GenerationSettings settings) {
        return new EarthInitContext(world, settings);
    }
}
