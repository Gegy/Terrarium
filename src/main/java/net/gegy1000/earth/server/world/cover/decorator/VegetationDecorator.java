package net.gegy1000.earth.server.world.cover.decorator;

import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.vegetation.Vegetation;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.terrarium.server.util.WeightedPool;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

import javax.annotation.Nullable;
import java.util.Random;

public final class VegetationDecorator implements CoverDecorator {
    private static final int CHUNK_AREA = 16 * 16;

    private static final NoiseGeneratorSimplex DENSITY_NOISE = new NoiseGeneratorSimplex(new Random(21));

    private final WeightedPool<Vegetation> pool = new WeightedPool<>();
    private float minDensity = 0.5F;
    private float maxDensity = 0.5F;
    private float area = 1.0F;

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    private final GrowthPredictors predictors = new GrowthPredictors();
    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();

    private float[] weightsBuffer;

    public VegetationDecorator withDensity(float min, float max) {
        this.minDensity = min;
        this.maxDensity = max;
        return this;
    }

    public VegetationDecorator withDensity(float density) {
        return this.withDensity(density, density);
    }

    public VegetationDecorator withRadius(float radius) {
        this.area = (float) (Math.PI * radius * radius);
        return this;
    }

    public VegetationDecorator add(Vegetation vegetation, float weight) {
        this.pool.add(vegetation, weight);
        this.weightsBuffer = new float[this.pool.size()];
        return this;
    }

    @Override
    public void decorate(ColumnDataCache dataCache, ChunkPopulationWriter writer, CubicPos cubePos, Random random) {
        if (this.pool.isEmpty()) return;

        World world = writer.getGlobal();
        int minX = cubePos.getCenterX();
        int minZ = cubePos.getCenterZ();

        float densityNoise = (float) (DENSITY_NOISE.getValue(cubePos.getX(), cubePos.getZ()) + 1.0F) / 2.0F;
        float density = this.minDensity + (this.maxDensity - this.minDensity) * densityNoise;

        int count = Math.round(density * CHUNK_AREA / this.area);

        for (int i = 0; i < count; i++) {
            int x = minX + random.nextInt(16);
            int z = minZ + random.nextInt(16);
            this.mutablePos.setPos(x, 0, z);

            if (!writer.getSurfaceMut(this.mutablePos)) continue;

            this.predictorSampler.sampleTo(dataCache, x, z, this.predictors);

            Vegetation vegetation = this.sample(random, this.predictors);
            if (vegetation != null) {
                double indicator = vegetation.getGrowthIndicator().evaluate(this.predictors);
                vegetation.getGenerator().generate(world, random, this.mutablePos, indicator);
            }
        }
    }

    @Nullable
    private Vegetation sample(Random random, GrowthPredictors predictors) {
        float totalWeight = 0.0F;
        for (int i = 0; i < this.pool.size(); i++) {
            WeightedPool.Entry<Vegetation> entry = this.pool.get(i);
            Vegetation vegetation = entry.getValue();
            double indicator = vegetation.getGrowthIndicator().evaluate(predictors);

            float weight = (float) (entry.getWeight() * indicator);
            this.weightsBuffer[i] = weight;
            totalWeight += weight;
        }

        float threshold = random.nextFloat() * totalWeight;

        float weight = 0.0F;
        for (int i = 0; i < this.pool.size(); i++) {
            WeightedPool.Entry<Vegetation> entry = this.pool.get(i);
            weight += this.weightsBuffer[i];
            if (weight > threshold) {
                return entry.getValue();
            }
        }

        return null;
    }
}
