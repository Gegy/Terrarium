package net.gegy1000.earth.server.world.ecology.vegetation;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.terrarium.server.util.WeightedPool;
import net.minecraft.util.math.BlockPos;
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

    public TreeDecorator setDensity(float min, float max) {
        this.minDensity = min;
        this.maxDensity = max;
        return this;
    }

    public TreeDecorator setDensity(float density) {
        return this.setDensity(density, density);
    }

    public TreeDecorator setRadius(float radius) {
        this.area = (float) (Math.PI * radius * radius);
        return this;
    }

    public TreeDecorator setArea(float area) {
        this.area = area;
        return this;
    }

    public void decorate(ChunkPopulationWriter writer, CubicPos cubePos, Random random) {
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

            VegetationGenerator vegetation = this.pool.sampleOrNull(random);
            if (vegetation != null) {
                vegetation.generate(world, random, this.mutablePos);
            }
        }
    }
}
