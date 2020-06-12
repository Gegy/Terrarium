package net.gegy1000.earth.server.world.composer.surface;

import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.soil.SoilSelector;
import net.gegy1000.earth.server.world.ecology.soil.SoilTexture;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class TerrainSurfaceComposer implements SurfaceComposer {
    private static final long SEED = 6035435416693430887L;
    private static final int MAX_SOIL_DEPTH = 6;

    private static final IBlockState AIR = Blocks.AIR.getDefaultState();

    private final NoiseGeneratorPerlin depthNoise;
    private double[] depthBuffer = new double[16 * 16];

    private final SpatialRandom random;

    private final IBlockState replaceBlock;

    private final GrowthPredictors predictors = new GrowthPredictors();
    private final GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public TerrainSurfaceComposer(
            World world,
            IBlockState replaceBlock
    ) {
        this.random = new SpatialRandom(world.getWorldInfo().getSeed(), SEED);
        this.depthNoise = new NoiseGeneratorPerlin(this.random, 4);

        this.replaceBlock = replaceBlock;
    }

    @Override
    public void composeSurface(TerrariumWorld terrarium, ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        data.with(EarthData.TERRAIN_HEIGHT).ifPresent(with -> {
            ShortRaster heightRaster = with.get(EarthData.TERRAIN_HEIGHT);

            if (!this.containsSurface(pos, heightRaster)) return;

            int minX = pos.getMinX();
            int minY = pos.getMinY();
            int minZ = pos.getMinZ();

            this.depthBuffer = this.depthNoise.getRegion(this.depthBuffer, minX, minZ, 16, 16, 0.0625, 0.0625, 1.0);

            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localX = 0; localX < 16; localX++) {
                    int x = localX + minX;
                    int z = localZ + minZ;

                    this.random.setSeed(x, minY, z);

                    this.predictorSampler.sampleTo(data, x, z, this.predictors);

                    int height = heightRaster.get(localX, localZ);
                    int slope = this.predictors.slope;

                    SoilTexture texture = SoilSelector.select(this.predictors);

                    double depthNoise = this.depthBuffer[localX + localZ * 16];

                    this.mutablePos.setPos(x, height, z);
                    this.coverColumn(texture, pos, writer, this.mutablePos, slope, (depthNoise + 4.0) / 8.0);
                }
            }
        });
    }

    private boolean containsSurface(CubicPos cubePos, ShortRaster heightRaster) {
        int minY = cubePos.getMinY();
        int maxY = cubePos.getMaxY() + MAX_SOIL_DEPTH;
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                short height = heightRaster.get(x, z);
                if (height >= minY && height <= maxY) {
                    return true;
                }
            }
        }
        return false;
    }

    private void coverColumn(SoilTexture texture, CubicPos pos, ChunkPrimeWriter writer, BlockPos surface, int slope, double depthNoise) {
        int x = surface.getX();
        int z = surface.getZ();

        int height = surface.getY();

        int minY = pos.getMinY();
        int maxY = pos.getMaxY();

        int currentDepth = -1;

        int soilDepth = MathHelper.floor(1.5 + this.random.nextDouble() * 0.25 + depthNoise * 1.5);
        if (height > maxY) {
            soilDepth = maxY - (height - soilDepth);
        }

        if (soilDepth <= 0) return;

        for (int y = Math.min(maxY, height + 1); y >= minY; y--) {
            IBlockState current = writer.get(x, y, z);
            while (current == AIR && --y >= 0) {
                current = writer.get(x, y, z);
                currentDepth = -1;
            }
            if (current == this.replaceBlock) {
                if (currentDepth == -1) {
                    currentDepth = soilDepth + 1;
                }
                if (currentDepth-- > 0) {
                    int depth = height - y;
                    this.mutablePos.setY(y);
                    IBlockState soil = texture.sample(this.random, this.mutablePos, slope, depth);
                    writer.set(x, y, z, soil);
                } else {
                    break;
                }
            }
        }
    }
}
