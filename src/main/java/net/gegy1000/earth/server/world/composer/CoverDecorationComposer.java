package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.world.EarthCoverDecoration;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;

public final class CoverDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 2492037454623254033L;

    private final SpatialRandom random;

    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();
    private final EnumRaster.Sampler<Cover> coverSampler = EnumRaster.sampler(EarthDataKeys.COVER, Cover.NO);

    private final GrowthPredictors predictors = new GrowthPredictors();

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    private static final WorldGenerator TALL_GRASS = new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);

    public CoverDecorationComposer(World world) {
        this.random = new SpatialRandom(world, DECORATION_SEED);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        int dataX = pos.getMaxX();
        int dataZ = pos.getMaxZ();

        Cover cover = this.coverSampler.sample(dataCache, dataX, dataZ);
        this.predictorSampler.sampleTo(dataCache, dataX, dataZ, this.predictors);

        EarthCoverDecoration.Builder builder = new EarthCoverDecoration.Builder(this.predictors);
        cover.configureDecorator(builder);

        EarthCoverDecoration biome = builder.build();
        biome.trees.decorate(writer, pos, this.random);

        this.generateGrass(writer, pos, biome);
    }

    private void generateGrass(ChunkPopulationWriter writer, CubicPos pos, EarthCoverDecoration biome) {
        int minX = pos.getCenterX();
        int minZ = pos.getCenterZ();

        World world = writer.getGlobal();

        int grassPerChunk = MathHelper.floor(biome.grassPerChunk);
        if (this.random.nextFloat() < biome.grassPerChunk - grassPerChunk) {
            grassPerChunk += 1;
        }

        for (int i = 0; i < grassPerChunk; i++) {
            this.mutablePos.setPos(
                    minX + this.random.nextInt(16),
                    0,
                    minZ + this.random.nextInt(16)
            );

            if (writer.getSurfaceMut(this.mutablePos)) {
                TALL_GRASS.generate(world, this.random, this.mutablePos);
            }
        }
    }
}
