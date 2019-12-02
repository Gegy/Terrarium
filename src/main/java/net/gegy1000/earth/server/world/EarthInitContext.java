package net.gegy1000.earth.server.world;

import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.LatLngCoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.LngLatCoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.ScaledCoordinateState;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.world.World;

import static net.gegy1000.earth.server.world.EarthWorldType.*;

public final class EarthInitContext {
    public final World world;
    public final GenerationSettings settings;

    public final double worldScale;

    public final CoordinateState latLngCoordinates;
    public final CoordinateState lngLatCoordinates;
    public final CoordinateState srtmRaster;
    public final CoordinateState landcoverRaster;
    public final CoordinateState climateRaster;

    private EarthInitContext(World world, GenerationSettings settings) {
        this.world = world;
        this.settings = settings;

        this.worldScale = 1.0 / settings.getDouble(WORLD_SCALE);
        this.latLngCoordinates = new LatLngCoordinateState(this.worldScale * SRTM_SCALE * 1200.0);
        this.lngLatCoordinates = new LngLatCoordinateState(this.worldScale * SRTM_SCALE * 1200.0);
        this.srtmRaster = new ScaledCoordinateState(this.worldScale * SRTM_SCALE);
        this.landcoverRaster = new ScaledCoordinateState(this.worldScale * LANDCOVER_SCALE);
        this.climateRaster = new ScaledCoordinateState(this.worldScale * CLIMATE_SCALE);
    }

    public static EarthInitContext from(World world, GenerationSettings settings) {
        return new EarthInitContext(world, settings);
    }
}
