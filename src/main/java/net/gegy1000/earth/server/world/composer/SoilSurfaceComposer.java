package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class SoilSurfaceComposer implements SurfaceComposer {
    private static final long SEED = 6035435416693430887L;
    private static final int MAX_SOIL_DEPTH = 6;

    private static final IBlockState AIR = Blocks.AIR.getDefaultState();

    private static final IBlockState GRASS = Blocks.GRASS.getDefaultState();
    private static final IBlockState DIRT = Blocks.DIRT.getDefaultState();

    private final NoiseGeneratorPerlin depthNoise;
    private double[] depthBuffer = new double[16 * 16];

    private final SpatialRandom random;

    private final IBlockState replaceBlock;

    public SoilSurfaceComposer(
            World world,
            IBlockState replaceBlock
    ) {
        this.random = new SpatialRandom(world.getWorldInfo().getSeed(), SEED);
        this.depthNoise = new NoiseGeneratorPerlin(this.random, 4);

        this.replaceBlock = replaceBlock;
    }

    @Override
    public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        // TODO: the only strictly required data is height

        data.with(EarthDataKeys.TERRAIN_HEIGHT, EarthDataKeys.SLOPE).ifPresent(with -> {
            ShortRaster heightRaster = with.get(EarthDataKeys.TERRAIN_HEIGHT);
            UByteRaster slopeRaster = with.get(EarthDataKeys.SLOPE);

            if (!this.containsSurface(pos, heightRaster)) return;

            int minX = pos.getMinX();
            int minY = pos.getMinY();
            int minZ = pos.getMinZ();

            this.depthBuffer = this.depthNoise.getRegion(this.depthBuffer, minX, minZ, 16, 16, 0.0625, 0.0625, 1.0);

            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    int height = heightRaster.get(x, z);

                    this.random.setSeed(x + minX, minY, z + minZ);

                    double depthNoise = this.depthBuffer[x + z * 16];
                    this.coverColumn(pos, writer, x, z, height, (depthNoise + 4.0) / 8.0);
                }
            }
        });
    }

    private boolean containsSurface(CubicPos cubePos, ShortRaster heightRaster) {
        int minY = cubePos.getMinY();
        int maxY = cubePos.getMaxY() + MAX_SOIL_DEPTH;
        for (int localZ = 0; localZ < heightRaster.getHeight(); localZ++) {
            for (int localX = 0; localX < heightRaster.getWidth(); localX++) {
                short height = heightRaster.get(localX, localZ);
                if (height >= minY && height <= maxY) {
                    return true;
                }
            }
        }
        return false;
    }

    private void coverColumn(CubicPos pos, ChunkPrimeWriter writer, int localX, int localZ, int height, double depthNoise) {
        int minY = pos.getMinY();
        int maxY = pos.getMaxY();

        int currentDepth = -1;

        int soilDepth = MathHelper.floor(1.0 + this.random.nextDouble() * 0.25 + depthNoise * 1.5);

        soilDepth = (maxY - height) + soilDepth;
        if (soilDepth <= 0) return;

        for (int y = Math.min(maxY, height + 1); y >= minY; y--) {
            IBlockState current = writer.get(localX, y, localZ);
            while (current == AIR && --y >= 0) {
                current = writer.get(localX, y, localZ);
                currentDepth = -1;
            }
            if (current == this.replaceBlock) {
                if (currentDepth == -1) {
                    currentDepth = soilDepth + 1;
                }
                if (currentDepth-- > 0) {
                    // TODO
                    writer.set(localX, y, localZ, height == y ? GRASS : DIRT);
                } else {
                    break;
                }
            }
        }
    }
}
