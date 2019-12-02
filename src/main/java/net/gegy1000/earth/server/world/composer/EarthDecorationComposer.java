package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverConfig;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;
import net.minecraft.world.World;

public class EarthDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 2492037454623254033L;

    private final SpatialRandom random;

    private final EnumRaster.Sampler<Cover> coverSampler;

    public EarthDecorationComposer(World world, DataKey<EnumRaster<Cover>> coverKey) {
        long seed = world.getWorldInfo().getSeed();
        this.random = new SpatialRandom(seed, DECORATION_SEED);

        this.coverSampler = EnumRaster.sampler(coverKey, Cover.NONE);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        Cover focus = this.coverSampler.sample(dataCache, pos.getMaxX(), pos.getMaxZ());

        CoverConfig config = focus.getConfig();
        config.decorators().forEach(decorator -> decorator.decorate(writer, pos, this.random));
    }
}
