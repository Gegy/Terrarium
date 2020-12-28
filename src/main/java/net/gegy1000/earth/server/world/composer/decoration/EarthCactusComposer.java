package net.gegy1000.earth.server.world.composer.decoration;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.writer.ChunkPopulationWriter;
import dev.gegy.gengen.util.SpatialRandom;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.maxent.MaxentGrowthIndicator;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenerator;

public final class EarthCactusComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 2492037454623254033L;

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
    public void composeDecoration(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer) {
        this.random.setSeed(pos.getCenterX(), pos.getCenterY(), pos.getCenterZ());

        int dataX = pos.getMaxX();
        int dataZ = pos.getMaxZ();

        // TODO: a better solution?
        if (writer.getCenterBiome() != Biomes.DESERT) {
            return;
        }

        this.predictorSampler.sampleTo(terrarium.getDataCache(), dataX, dataZ, this.predictors);
        double indicator = INDICATOR.evaluate(this.predictors);

        if (indicator > 0.65) {
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
