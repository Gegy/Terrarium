package net.gegy1000.earth.server.world.cover;

import net.gegy1000.terrarium.server.world.chunk.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.UnsignedByteRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;

public class EarthCoverContext implements CoverGenerationContext {
    private static final long ZONE_SCATTER_SEED = 2016944452769570983L;

    private final World world;
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final RegionComponentType<CoverRasterTile> coverComponent;
    private final RegionComponentType<UnsignedByteRasterTile> slopeComponent;

    private ShortRasterTile heightTile;
    private CoverRasterTile coverTile;
    private UnsignedByteRasterTile slopeTile;

    private final CoordinateState latLngCoordinate;

    private final PseudoRandomMap zoneScatterMap;

    private final boolean scatterZone;

    public EarthCoverContext(
            World world,
            RegionComponentType<ShortRasterTile> heightComponent,
            RegionComponentType<CoverRasterTile> coverComponent,
            RegionComponentType<UnsignedByteRasterTile> slopeComponent,
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
    public ShortRasterTile getHeightRaster() {
        return this.heightTile;
    }

    @Override
    public CoverRasterTile getCoverRaster() {
        return this.coverTile;
    }

    public UnsignedByteRasterTile getSlopeRaster() {
        return this.slopeTile;
    }

    public LatitudinalZone getZone(int globalX, int globalZ) {
        this.zoneScatterMap.initPosSeed(globalX, globalZ);

        int offsetX = this.scatterZone ? this.zoneScatterMap.nextInt(128) - this.zoneScatterMap.nextInt(128) : 0;
        int offsetZ = this.scatterZone ? this.zoneScatterMap.nextInt(128) - this.zoneScatterMap.nextInt(128) : 0;

        double latitude = this.latLngCoordinate.getX(globalX + offsetX, globalZ + offsetZ);
        return LatitudinalZone.get(latitude);
    }

    public CoordinateState getLatLngCoordinate() {
        return this.latLngCoordinate;
    }
}
