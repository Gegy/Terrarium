package net.gegy1000.earth.server.world.cover.decorator;

import net.gegy1000.earth.server.world.ecology.vegetation.Vegetation;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.terrarium.server.util.WeightedPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

import java.util.Random;

public final class VegetationDecorator implements CoverDecorator {
    private static final int CHUNK_AREA = 16 * 16;

    private static final NoiseGeneratorSimplex DENSITY_NOISE = new NoiseGeneratorSimplex(new Random(1));

    private final WeightedPool<Vegetation> pool;
    private final float minDensity;
    private final float maxDensity;

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    private VegetationDecorator(WeightedPool<Vegetation> pool, float minDensity, float maxDensity) {
        this.pool = pool;
        this.minDensity = minDensity;
        this.maxDensity = maxDensity;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void decorate(ChunkPopulationWriter writer, CubicPos cubePos, Random random) {
        World world = writer.getGlobal();

        float noise = (float) (DENSITY_NOISE.getValue(cubePos.getX(), cubePos.getZ()) + 1.0F) / 2.0F;
        float density = this.minDensity + (this.maxDensity - this.minDensity) * noise;

        int count = Math.round(density * CHUNK_AREA);

        for (int i = 0; i < count; i++) {
            Vegetation vegetation = this.pool.sample(random);

            int offsetX = random.nextInt(16);
            int offsetZ = random.nextInt(16);

            this.mutablePos.setPos(cubePos.getCenterX() + offsetX, 0, cubePos.getCenterZ() + offsetZ);

            if (!writer.getSurfaceMut(this.mutablePos)) continue;

            vegetation.getGenerator().generate(world, random, this.mutablePos);
        }
    }

    public static class Builder {
        private final WeightedPool.Builder<Vegetation> vegetation = WeightedPool.builder();

        private float radius = 1.0F;

        private float minDensity = 0.3F;
        private float maxDensity = 0.5F;

        private Builder() {
        }

        public Builder add(Vegetation vegetation, float weight) {
            this.vegetation.add(vegetation, weight);
            return this;
        }

        public Builder add(WeightedPool<Vegetation> pool) {
            for (WeightedPool.Entry<Vegetation> entry : pool) {
                this.vegetation.add(entry.getValue(), entry.getWeight());
            }
            return this;
        }

        public Builder radius(float radius) {
            this.radius = radius;
            return this;
        }

        public Builder density(float minDensity, float maxDensity) {
            this.minDensity = minDensity;
            this.maxDensity = maxDensity;
            return this;
        }

        public VegetationDecorator build() {
            float area = (float) (Math.PI * this.radius * this.radius);
            float minDensity = this.minDensity / area;
            float maxDensity = this.maxDensity / area;
            return new VegetationDecorator(this.vegetation.build(), minDensity, maxDensity);
        }
    }
}
