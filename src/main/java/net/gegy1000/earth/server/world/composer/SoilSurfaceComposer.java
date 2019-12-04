package net.gegy1000.earth.server.world.composer;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Optional;

public class SoilSurfaceComposer implements SurfaceComposer {
    private static final long SEED = 6035435416693430887L;
    private static final int MAX_SOIL_DEPTH = 6;

    private static final IBlockState AIR = Blocks.AIR.getDefaultState();

    private static final IBlockState GRASS = Blocks.GRASS.getDefaultState();
    private static final IBlockState DIRT = Blocks.DIRT.getDefaultState();

    private final NoiseGeneratorPerlin depthNoise;
    private double[] depthBuffer = new double[16 * 16];

    private final SpatialRandom random;

    private final DataKey<ShortRaster> heightKey;
    private final DataKey<UByteRaster> slopeKey;

    private final IBlockState replaceBlock;

    public SoilSurfaceComposer(
            World world,
            DataKey<ShortRaster> heightKey,
            DataKey<UByteRaster> slopeKey,
            IBlockState replaceBlock
    ) {
        this.random = new SpatialRandom(world.getWorldInfo().getSeed(), SEED);
        this.depthNoise = new NoiseGeneratorPerlin(this.random, 4);

        this.heightKey = heightKey;
        this.slopeKey = slopeKey;

        this.replaceBlock = replaceBlock;
    }

    @Override
    public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        int globalX = pos.getMinX();
        int globalY = pos.getMinY();
        int globalZ = pos.getMinZ();

        Optional<ShortRaster> heightOption = data.get(this.heightKey);
        if (!heightOption.isPresent()) return;

        ShortRaster heightRaster = heightOption.get();
        if (!this.containsSurface(pos, heightRaster)) return;

        Optional<UByteRaster> slopeOption = data.get(this.slopeKey);
        if (!slopeOption.isPresent()) return;

        UByteRaster slopeRaster = slopeOption.get();

        this.depthBuffer = this.depthNoise.getRegion(this.depthBuffer, globalX, globalZ, 16, 16, 0.0625, 0.0625, 1.0);

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int height = heightRaster.get(localX, localZ);
                if (pos.getMinY() <= height) {
                    this.random.setSeed(localX + globalX, globalY, localZ + globalZ);

                    double depthNoise = this.depthBuffer[localX + localZ * 16];
                    this.coverColumn(pos, writer, localX, localZ, height, depthNoise);
                }
            }
        }
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
        int maxY = Math.min(pos.getMaxY(), height);

        int depth = -1;
        int soilDepth = Math.max((int) (depthNoise / 3.0 + 3.0 + this.random.nextDouble() * 0.25), 1);
        soilDepth = maxY - (height - soilDepth);
        if (soilDepth <= 0) {
            return;
        }

        for (int y = maxY; y >= minY; y--) {
            IBlockState current = writer.get(localX, y, localZ);
            while (current == AIR && --y >= 0) {
                current = writer.get(localX, y, localZ);
                depth = -1;
            }
            if (current == this.replaceBlock) {
                if (depth == -1) {
                    depth = soilDepth + 1;
                }
                if (depth-- > 0) {
                    // TODO
                    writer.set(localX, y, localZ, height == y ? GRASS : DIRT);
                } else {
                    break;
                }
            }
        }
    }
}
