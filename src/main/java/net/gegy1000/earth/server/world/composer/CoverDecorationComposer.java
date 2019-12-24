package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.minecraft.world.World;

public class CoverDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 2492037454623254033L;

    private final SpatialRandom random;

    private final EnumRaster.Sampler<Cover> coverSampler = EnumRaster.sampler(EarthDataKeys.COVER, Cover.NO);

    public CoverDecorationComposer(World world) {
        this.random = new SpatialRandom(world, DECORATION_SEED);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        Cover cover = this.coverSampler.sample(dataCache, pos.getMaxX(), pos.getMaxZ());

        cover.getConfig().decorators().forEach(decorator -> {
            decorator.decorate(dataCache, writer, pos, this.random);
        });
    }
}
