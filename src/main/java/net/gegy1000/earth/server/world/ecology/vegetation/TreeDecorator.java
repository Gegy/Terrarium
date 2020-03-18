package net.gegy1000.earth.server.world.ecology.vegetation;

import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.terrarium.server.util.WeightedPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

import java.util.Random;

public final class TreeDecorator {
    private static final int CHUNK_AREA = 16 * 16;

    private static final NoiseGeneratorSimplex DENSITY_NOISE = new NoiseGeneratorSimplex(new Random(21));

    private final WeightedPool<VegetationGenerator> pool;
    private float minDensity = 0.5F;
    private float maxDensity = 0.5F;
    private float area = 1.0F;

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public TreeDecorator(WeightedPool<VegetationGenerator> pool) {
        this.pool = pool;
    }

    public void decorate(ChunkPopulationWriter writer, CubicPos cubePos, Random random) {
        if (this.pool.isEmpty()) return;

        World world = writer.getGlobal();
        int minX = cubePos.getCenterX();
        int minZ = cubePos.getCenterZ();

        float densityNoise = (float) (DENSITY_NOISE.getValue(cubePos.getX(), cubePos.getZ()) + 1.0F) / 2.0F;
        float density = this.minDensity + (this.maxDensity - this.minDensity) * densityNoise;

        float fCount = density * CHUNK_AREA / this.area;

        int count = MathHelper.floor(fCount);
        float remainder = fCount - count;
        if (random.nextFloat() < remainder) {
            count += 1;
        }

        for (int i = 0; i < count; i++) {
            int x = minX + random.nextInt(16);
            int z = minZ + random.nextInt(16);
            this.mutablePos.setPos(x, 0, z);

            if (!writer.getSurfaceMut(this.mutablePos)) continue;

            VegetationGenerator vegetation = this.pool.sampleOrNull(random);
            if (vegetation != null) {
                vegetation.generate(world, random, this.mutablePos);
            }
        }
    }

    public static class Builder {
        private static final double SUITABILITY_THRESHOLD = 0.85;

        private final GrowthPredictors predictors;

        private final WeightedPool<VegetationGenerator> pool = new WeightedPool<>();

        private VegetationGenerator mostSuitable;
        private double mostSuitableIndicator;

        private float area = 1.0F;
        private float minDensity = 0.0F;
        private float maxDensity = 0.2F;

        public Builder(GrowthPredictors predictors) {
            this.predictors = predictors;
        }

        public Builder addCandidate(Vegetation vegetation) {
            double indicator = vegetation.getGrowthIndicator().evaluate(this.predictors);
            if (indicator > SUITABILITY_THRESHOLD) {
                this.pool.add(vegetation.getGenerator(), (float) indicator);
            }

            if (indicator > this.mostSuitableIndicator) {
                this.mostSuitable = vegetation.getGenerator();
                this.mostSuitableIndicator = indicator;
            }

            return this;
        }

        public Builder setDensity(float minDensity, float maxDensity) {
            this.minDensity = minDensity;
            this.maxDensity = maxDensity;
            return this;
        }

        public Builder setRadius(float radius) {
            this.area = (float) (Math.PI * radius * radius);
            return this;
        }

        public Builder setArea(float area) {
            this.area = area;
            return this;
        }

        public TreeDecorator build() {
            if (this.pool.isEmpty() && this.mostSuitable != null) {
                this.pool.add(this.mostSuitable, (float) this.mostSuitableIndicator);
            }

            TreeDecorator decorator = new TreeDecorator(this.pool);
            decorator.minDensity = this.minDensity;
            decorator.maxDensity = this.maxDensity;
            decorator.area = this.area;

            return decorator;
        }
    }
}
