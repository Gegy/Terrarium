package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.world.pipeline.source.GoogleGeocoder;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkCompositionProcedure;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.source.Geocoder;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

import javax.annotation.Nullable;

public class EarthGeneratorConfig extends TerrariumGeneratorConfig {
    private final CoordinateState geoCoordinate;
    private final Geocoder geocoder;

    public EarthGeneratorConfig(
            GenerationSettings settings,
            RegionGenerationHandler regionHandler,
            ChunkCompositionProcedure<?> compositionProcedure,
            Coordinate spawnPosition,
            CoordinateState geoCoordinate
    ) {
        super(settings, regionHandler, compositionProcedure, spawnPosition);
        this.geoCoordinate = geoCoordinate;
        this.geocoder = new GoogleGeocoder();
    }

    @Nullable
    public static EarthGeneratorConfig get(IWorld world) {
        ChunkGenerator<?> generator = world.getChunkManager().getChunkGenerator();
        if (generator == null) {
            return null;
        }

        ChunkGeneratorSettings config = generator.getSettings();
        if (config instanceof EarthGeneratorConfig) {
            return (EarthGeneratorConfig) config;
        }

        return null;
    }

    public Geocoder getGeocoder() {
        return this.geocoder;
    }

    public CoordinateState getGeoCoordinate() {
        return this.geoCoordinate;
    }

    public double getLatitude(double x, double z) {
        return this.geoCoordinate.getX(x, z);
    }

    public double getLongitude(double x, double z) {
        return this.geoCoordinate.getZ(x, z);
    }

    public double getX(double latitude, double longitude) {
        return this.geoCoordinate.getBlockX(latitude, longitude);
    }

    public double getZ(double latitude, double longitude) {
        return this.geoCoordinate.getBlockZ(latitude, longitude);
    }
}
