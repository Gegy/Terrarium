package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.CoverClassification;
import net.gegy1000.earth.server.world.cover.CoverConfig;
import net.gegy1000.earth.server.world.pipeline.source.tile.CoverRaster;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;

public class EarthCarvingComposer implements SurfaceComposer {
    private final RegionComponentType<CoverRaster> coverComponent;

    public EarthCarvingComposer(RegionComponentType<CoverRaster> coverComponent) {
        this.coverComponent = coverComponent;
    }

    @Override
    public void composeSurface(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPrimeWriter writer) {
        CoverRaster coverRaster = regionHandler.getChunkRaster(this.coverComponent);
        CoverClassification focus = coverRaster.get(15, 15);

        CoverConfig config = focus.getConfig();
        config.carvers().forEach(carver -> carver.carve(pos, writer, regionHandler.getChunkRasters()));
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.coverComponent };
    }
}
