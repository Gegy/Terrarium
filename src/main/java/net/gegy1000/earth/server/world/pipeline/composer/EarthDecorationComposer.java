package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.earth.server.world.cover.CoverClassification;
import net.gegy1000.earth.server.world.cover.CoverConfig;
import net.gegy1000.earth.server.world.pipeline.source.tile.CoverRaster;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;

import java.util.Random;

public class EarthDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 2492037454623254033L;

    private final Random random;
    private final PseudoRandomMap coverMap;

    private final RegionComponentType<CoverRaster> coverComponent;

    public EarthDecorationComposer(World world, RegionComponentType<CoverRaster> coverComponent) {
        long seed = world.getWorldInfo().getSeed();

        this.random = new Random(seed ^ DECORATION_SEED);
        this.coverMap = new PseudoRandomMap(seed, this.random.nextLong());

        this.coverComponent = coverComponent;
    }

    @Override
    public void composeDecoration(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPopulationWriter writer) {
        this.coverMap.initPosSeed(pos.getMinX(), pos.getMinY(), pos.getMinZ());
        this.random.setSeed(this.coverMap.next());

        CoverRaster coverRaster = regionHandler.getChunkRaster(this.coverComponent);
        CoverClassification focus = coverRaster.get(15, 15);

        CoverConfig config = focus.getConfig();
        config.decorators().forEach(decorator -> decorator.decorate(writer, pos, this.random));
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.coverComponent };
    }
}
