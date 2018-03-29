package net.gegy1000.earth.server.world.cover;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.world.World;

public class EarthCoverContext implements CoverGenerationContext {
    private final World world;
    private final ShortRasterTileAccess heightRaster;
    private final CoverRasterTileAccess coverRaster;
    private final ByteRasterTileAccess slopeRaster;
    private final CoordinateState latLngCoordinate;

    public EarthCoverContext(
            World world, TerrariumWorldData worldData,
            RegionComponentType<ShortRasterTileAccess> heightComponent,
            RegionComponentType<CoverRasterTileAccess> coverComponent,
            RegionComponentType<ByteRasterTileAccess> slopeComponent,
            CoordinateState latLngCoordinate) {
        this.world = world;
        this.heightRaster = this.getRaster(worldData, heightComponent);
        this.coverRaster = this.getRaster(worldData, coverComponent);
        this.slopeRaster = this.getRaster(worldData, slopeComponent);
        this.latLngCoordinate = latLngCoordinate;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public long getSeed() {
        return this.world.getWorldInfo().getSeed();
    }

    @Override
    public ShortRasterTileAccess getHeightRaster() {
        return this.heightRaster;
    }

    @Override
    public CoverRasterTileAccess getCoverRaster() {
        return this.coverRaster;
    }

    public ByteRasterTileAccess getSlopeRaster() {
        return this.slopeRaster;
    }

    public CoordinateState getLatLngCoordinate() {
        return this.latLngCoordinate;
    }

    public static class Parser implements InstanceObjectParser<CoverGenerationContext> {
        @Override
        public CoverGenerationContext parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            RegionComponentType<ShortRasterTileAccess> heightComponent = valueParser.parseComponentType(objectRoot, "height_component", ShortRasterTileAccess.class);
            RegionComponentType<CoverRasterTileAccess> coverComponent = valueParser.parseComponentType(objectRoot, "cover_component", CoverRasterTileAccess.class);
            RegionComponentType<ByteRasterTileAccess> slopeComponent = valueParser.parseComponentType(objectRoot, "slope_component", ByteRasterTileAccess.class);
            CoordinateState latLngCoordinate = valueParser.parseCoordinateState(objectRoot, "lat_lng_coordinate");

            return new EarthCoverContext(world, worldData, heightComponent, coverComponent, slopeComponent, latLngCoordinate);
        }
    }
}
