package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.maxent.MaxentGrowthIndicator;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenerator;

public final class EarthCactusComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 2492037454623254033L;
    private static final double THRESHOLD = 0.95;

    private static final GrowthIndicator INDICATOR = MaxentGrowthIndicator.tryParse(new ResourceLocation(TerrariumEarth.ID, "vegetation/models/cactus.lambdas"))
            .orElse(GrowthIndicator.no());

    private static final WorldGenerator CACTUS = new WorldGenCactus();

    private final SpatialRandom random;

    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();
    private final GrowthPredictors predictors = new GrowthPredictors();

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public EarthCactusComposer(World world) {
        this.random = new SpatialRandom(world, DECORATION_SEED);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        int dataX = pos.getMaxX();
        int dataZ = pos.getMaxZ();

        this.predictorSampler.sampleTo(dataCache, dataX, dataZ, this.predictors);
        double indicator = INDICATOR.evaluate(this.predictors);

        if (indicator > THRESHOLD) {
            this.generateCacti(writer, pos);
        }
    }

    private void generateCacti(ChunkPopulationWriter writer, CubicPos pos) {
        int minX = pos.getCenterX();
        int minZ = pos.getCenterZ();

        this.mutablePos.setPos(
                minX + this.random.nextInt(16),
                0,
                minZ + this.random.nextInt(16)
        );

        if (writer.getSurfaceMut(this.mutablePos)) {
            CACTUS.generate(writer.getGlobal(), this.random, this.mutablePos);
        }
    }
}
