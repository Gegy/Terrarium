package net.gegy1000.earth.server.world.ores;

import net.gegy1000.gengen.api.CubicPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Random;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface OreDistribution {
    int VANILLA_SURFACE_Y = 62;

    Stream<BlockPos> forChunk(CubicPos cubePos, int surfaceHeight, Random random);

    static OreDistribution vanillaUniform(int count, int maxY) {
        double maxCubeY = maxY / 16.0;
        double countPerCube = count / maxCubeY;
        return OreDistribution.uniform(countPerCube, maxY - VANILLA_SURFACE_Y);
    }

    static OreDistribution vanillaBand(int count, int centerY, int spreadY) {
        int minY = centerY - spreadY;
        int maxY = centerY + spreadY;

        double cubeRangeY = (maxY - minY) / 16.0;
        double countPerCube = count / cubeRangeY;

        return band(countPerCube, minY - VANILLA_SURFACE_Y, maxY - VANILLA_SURFACE_Y);
    }

    static OreDistribution band(double countPerCube, int minY, int maxY) {
        return uniformWithin(countPerCube, y -> y >= minY && y <= maxY);
    }

    static OreDistribution uniform(double countPerCube, int maxY) {
        return uniformWithin(countPerCube, y -> y <= maxY);
    }

    static OreDistribution uniformWithin(double countPerCube, IntPredicate validY) {
        return (cubePos, surfaceHeight, random) -> {
            BlockPos cubeOrigin = new BlockPos(cubePos.getCenterX(), cubePos.getCenterY(), cubePos.getCenterZ());

            int minRelativeY = cubeOrigin.getY() - surfaceHeight;
            int maxRelativeY = cubeOrigin.getY() + 16 - surfaceHeight;
            if (!validY.test(minRelativeY) && !validY.test(maxRelativeY)) {
                return Stream.empty();
            }

            int count = MathHelper.floor(countPerCube);
            double countFract = countPerCube - count;
            if (random.nextFloat() < countFract) count += 1;

            if (count <= 0) return Stream.empty();

            return IntStream.range(0, count)
                    .mapToObj(v -> cubeOrigin.add(
                            random.nextInt(16),
                            random.nextInt(16),
                            random.nextInt(16)
                    ))
                    .filter(pos -> validY.test(pos.getY() - surfaceHeight));
        };
    }
}
