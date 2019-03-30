package net.gegy1000.earth.server.world.cover;

import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.CoverRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;

public class EarthCoverContext implements CoverGenerationContext {
    private static final long ZONE_SCATTER_SEED = 2016944452769570983L;

    private final World world;
    private final RegionComponentType<ShortRaster> heightComponent;
    private final RegionComponentType<CoverRaster> coverComponent;
    private final RegionComponentType<UnsignedByteRaster> slopeComponent;

    private ShortRaster heightTile;
    private CoverRaster coverTile;
    private UnsignedByteRaster slopeTile;

    private final CoordinateState latLngCoordinate;

    private final PseudoRandomMap zoneScatterMap;

    private final boolean scatterZone;

    public EarthCoverContext(
            World world,
            RegionComponentType<ShortRaster> heightComponent,
            RegionComponentType<CoverRaster> coverComponent,
            RegionComponentType<UnsignedByteRaster> slopeComponent,
            CoordinateState latLngCoordinate,
            boolean scatterZone) {
        this.world = world;
        this.heightComponent = heightComponent;
        this.coverComponent = coverComponent;
        this.slopeComponent = slopeComponent;
        this.latLngCoordinate = latLngCoordinate;

        this.zoneScatterMap = new PseudoRandomMap(world, ZONE_SCATTER_SEED);
        this.scatterZone = scatterZone;
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
    public void prepareChunk(RegionGenerationHandler regionHandler) {
        this.heightTile = regionHandler.getCachedChunkRaster(this.heightComponent);
        this.coverTile = regionHandler.getCachedChunkRaster(this.coverComponent);
        this.slopeTile = regionHandler.getCachedChunkRaster(this.slopeComponent);
    }

    @Override
    public ShortRaster getHeightRaster() {
        return this.heightTile;
    }

    @Override
    public CoverRaster getCoverRaster() {
        return this.coverTile;
    }

    public UnsignedByteRaster getSlopeRaster() {
        return this.slopeTile;
    }

    public LatitudinalZone getZone(int globalX, int globalZ) {
        this.zoneScatterMap.initPosSeed(globalX, globalZ);

        int offsetX = this.scatterZone ? this.zoneScatterMap.nextInt(128) - this.zoneScatterMap.nextInt(128) : 0;
        int offsetZ = this.scatterZone ? this.zoneScatterMap.nextInt(128) - this.zoneScatterMap.nextInt(128) : 0;

        double latitude = this.latLngCoordinate.getX(globalX + offsetX, globalZ + offsetZ);
        return LatitudinalZone.get(latitude);
    }

    public LatitudinalZone getZone(CubicPos pos) {
        return this.getZone(pos.getMinX() + 16, pos.getMinZ() + 16);
    }

    public CoordinateState getLatLngCoordinate() {
        return this.latLngCoordinate;
    }
}
