package net.gegy1000.earth.server.world.ecology.vegetation;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.terrarium.server.util.WeightedPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Random;

public final class FlowerDecorator {
    private static final NoiseGeneratorPerlin SAMPLE_NOISE = new NoiseGeneratorPerlin(new Random(123), 1);

    private final WeightedPool<VegetationGenerator> pool = new WeightedPool<>();
    private float countPerChunk = 1.0F;

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public FlowerDecorator setCountPerChunk(float countPerChunk) {
        this.countPerChunk = countPerChunk;
        return this;
    }

    public FlowerDecorator add(VegetationGenerator vegetation, float weight) {
        this.pool.add(vegetation, weight);
        return this;
    }

    public void decorate(ChunkPopulationWriter writer, CubicPos cubePos, Random random) {
        if (this.pool.isEmpty()) return;

        World world = writer.getGlobal();
        int minX = cubePos.getCenterX();
        int minZ = cubePos.getCenterZ();

        int count = MathHelper.floor(this.countPerChunk);
        float remainder = this.countPerChunk - count;
        if (random.nextFloat() < remainder) {
            count += 1;
        }

        for (int i = 0; i < count; i++) {
            int x = minX + random.nextInt(16);
            int z = minZ + random.nextInt(16);
            this.mutablePos.setPos(x, 0, z);

            if (!writer.getSurfaceMut(this.mutablePos)) continue;

            VegetationGenerator vegetation = this.pool.sampleOrNull(noise(x, z));
            if (vegetation != null) {
                vegetation.generate(world, random, this.mutablePos);
            }
        }
    }

    private static float noise(double x, double z) {
        float noise = (float) SAMPLE_NOISE.getValue(x / 48.0, z / 48.0);
        return MathHelper.clamp((1.0F + noise) / 2.0F, 0.0F, 1.0F);
    }
}
