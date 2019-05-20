package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverConfig;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;
import net.minecraft.world.World;

import java.util.Random;

public class EarthDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 2492037454623254033L;

    private final Random random;
    private final PseudoRandomMap coverMap;

    private final EnumRaster.Sampler<Cover> coverSampler;

    public EarthDecorationComposer(World world, DataKey<EnumRaster<Cover>> coverKey) {
        long seed = world.getWorldInfo().getSeed();

        this.random = new Random(seed ^ DECORATION_SEED);
        this.coverMap = new PseudoRandomMap(seed, this.random.nextLong());

        this.coverSampler = EnumRaster.sampler(coverKey, Cover.NONE);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        this.coverMap.initPosSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());
        this.random.setSeed(this.coverMap.next());

        Cover focus = this.coverSampler.sample(dataCache, pos.getMaxX(), pos.getMaxZ());

        CoverConfig config = focus.getConfig();
        config.decorators().forEach(decorator -> decorator.decorate(writer, pos, this.random));
    }
}
