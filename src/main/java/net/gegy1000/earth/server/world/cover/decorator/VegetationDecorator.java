package net.gegy1000.earth.server.world.cover.decorator;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.ecology.AbioticComponents;
import net.gegy1000.earth.server.world.ecology.vegetation.Vegetation;
import net.gegy1000.terrarium.server.util.WeightedPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public final class VegetationDecorator implements CoverDecorator {
    private static final int CHUNK_AREA = 16 * 16;

    private final WeightedPool<Vegetation> pool;
    private final int minCount;
    private final int maxCount;

    private final AbioticComponents abiotic = new AbioticComponents();

    private VegetationDecorator(WeightedPool<Vegetation> pool, float minDensity, float maxDensity) {
        this.pool = pool;
        this.minCount = MathHelper.floor(CHUNK_AREA * minDensity);
        this.maxCount = MathHelper.ceil(CHUNK_AREA * maxDensity);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void decorate(ChunkPopulationWriter writer, CubicPos cubePos, Random random) {
        WeightedPool<Vegetation> pool = this.buildTweakedPool();
        if (pool.isEmpty()) {
            return;
        }

        World world = writer.getGlobal();

        int count = random.nextInt(this.maxCount - this.minCount + 1) + this.minCount;
        for (int i = 0; i < count; i++) {
            Vegetation vegetation = this.pool.sample(random);

            int offsetX = random.nextInt(16);
            int offsetZ = random.nextInt(16);

            BlockPos surface = writer.getSurface(cubePos.getCenter().add(offsetX, 0, offsetZ));
            if (surface == null) continue;

            vegetation.getGenerator().generate(world, random, surface);
        }
    }

    private WeightedPool<Vegetation> buildTweakedPool() {
        WeightedPool.Builder<Vegetation> poolBuilder = WeightedPool.builder();

        for (WeightedPool.Entry<Vegetation> entry : this.pool) {
            Vegetation vegetation = entry.getValue();
            float weight = entry.getWeight();

            // TODO: set abiotic values
            double growth = vegetation.getGrowthIndicator().test(this.abiotic);

            float tweakedWeight = (float) (weight * growth);
            if (tweakedWeight > 0.0F) {
                poolBuilder = poolBuilder.with(vegetation, tweakedWeight);
            }
        }

        return poolBuilder.build();
    }

    public static class Builder {
        private final WeightedPool.Builder<Vegetation> vegetation = WeightedPool.builder();

        private float radius = 1.0F;

        private float minDensity = 0.3F;
        private float maxDensity = 0.5F;

        private Builder() {
        }

        public Builder withVegetation(Vegetation vegetation, float weight) {
            this.vegetation.with(vegetation, weight);
            return this;
        }

        public Builder withVegetation(WeightedPool<Vegetation> pool) {
            for (WeightedPool.Entry<Vegetation> entry : pool) {
                this.vegetation.with(entry.getValue(), entry.getWeight());
            }
            return this;
        }

        public Builder withRadius(float radius) {
            this.radius = radius;
            return this;
        }

        public Builder withDensity(float minDensity, float maxDensity) {
            this.minDensity = minDensity;
            this.maxDensity = maxDensity;
            return this;
        }

        public VegetationDecorator build() {
            float diameter = this.radius * 2.0F;
            float scaleFactor = 1.0F / (diameter * diameter);
            float minDensity = this.minDensity * scaleFactor;
            float maxDensity = this.maxDensity * scaleFactor;
            return new VegetationDecorator(this.vegetation.build(), minDensity, maxDensity);
        }
    }
}
