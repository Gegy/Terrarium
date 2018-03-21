package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.world.World;

public class CoverGenerationContext {
    private final World world;

    private final ShortRasterTileAccess heightRaster;
    private final CoverRasterTileAccess coverRaster;
    private final ByteRasterTileAccess slopeRaster;

    public CoverGenerationContext(World world, ShortRasterTileAccess heightRaster, CoverRasterTileAccess coverRaster, ByteRasterTileAccess slopeRaster) {
        this.world = world;
        this.heightRaster = heightRaster;
        this.coverRaster = coverRaster;
        this.slopeRaster = slopeRaster;
    }

    public World getWorld() {
        return this.world;
    }

    public long getSeed() {
        return this.world.getWorldInfo().getSeed();
    }

    public ShortRasterTileAccess getHeightRaster() {
        return this.heightRaster;
    }

    public CoverRasterTileAccess getCoverRaster() {
        return this.coverRaster;
    }

    public ByteRasterTileAccess getSlopeRaster() {
        return this.slopeRaster;
    }
}
