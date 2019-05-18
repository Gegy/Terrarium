package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.earth.server.world.soil.SoilConfig;
import net.gegy1000.earth.server.world.soil.horizon.SoilHorizonConfig;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Optional;
import java.util.Random;

public class SoilSurfaceComposer implements SurfaceComposer {
    private static final long SEED = 6035435416693430887L;

    private static final IBlockState AIR = Blocks.AIR.getDefaultState();

    private final NoiseGeneratorPerlin depthNoise;
    private double[] depthBuffer = new double[16 * 16];

    private final Random random;
    private final PseudoRandomMap coverMap;

    private final DataKey<ShortRaster> heightKey;
    private final DataKey<ObjRaster<SoilConfig>> soilKey;

    private final IBlockState replaceBlock;

    public SoilSurfaceComposer(
            World world,
            DataKey<ShortRaster> heightKey,
            DataKey<ObjRaster<SoilConfig>> soilKey,
            IBlockState replaceBlock
    ) {
        this.random = new Random(world.getWorldInfo().getSeed() ^ SEED);
        this.depthNoise = new NoiseGeneratorPerlin(this.random, 4);
        this.coverMap = new PseudoRandomMap(world.getWorldInfo().getSeed(), this.random.nextLong());

        this.heightKey = heightKey;
        this.soilKey = soilKey;

        this.replaceBlock = replaceBlock;
    }

    @Override
    public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        int globalX = pos.getMinX();
        int globalY = pos.getMinY();
        int globalZ = pos.getMinZ();

        Optional<ShortRaster> heightOption = data.get(this.heightKey);
        Optional<ObjRaster<SoilConfig>> soilOption = data.get(this.soilKey);

        if (!heightOption.isPresent() || !soilOption.isPresent()) {
            return;
        }

        ShortRaster heightRaster = heightOption.get();
        ObjRaster<SoilConfig> soilRaster = soilOption.get();

        this.depthBuffer = this.depthNoise.getRegion(this.depthBuffer, globalX, globalZ, 16, 16, 0.0625, 0.0625, 1.0);

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int height = heightRaster.get(localX, localZ);
                if (pos.getMinY() <= height) {
                    this.coverMap.initPosSeed(localX + globalX, globalY, localZ + globalZ);
                    this.random.setSeed(this.coverMap.next());

                    SoilConfig soilConfig = soilRaster.get(localX, localZ);
                    this.coverColumn(soilConfig, pos, writer, localX, localZ, height, this.depthBuffer[localX + localZ * 16]);
                }
            }
        }
    }

    private void coverColumn(SoilConfig config, CubicPos pos, ChunkPrimeWriter writer, int localX, int localZ, int height, double depthNoise) {
        int minY = pos.getMinY();
        int maxY = Math.min(pos.getMaxY(), height);

        int minX = pos.getMinX();
        int minZ = pos.getMinZ();

        int depth = -1;
        int soilDepth = Math.max((int) (depthNoise / 3.0 + 3.0 + this.coverMap.nextDouble() * 0.25), 1);
        soilDepth = maxY - (height - soilDepth);
        if (soilDepth <= 0) {
            return;
        }

        for (int localY = maxY; localY >= minY; localY--) {
            IBlockState current = writer.get(localX, localY, localZ);
            while (current == AIR && --localY >= 0) {
                current = writer.get(localX, localY, localZ);
                depth = -1;
            }
            if (current == this.replaceBlock) {
                SoilHorizonConfig horizon = config.getHorizon(height - localY);
                if (depth == -1) {
                    depth = soilDepth + 1;
                }
                if (depth-- > 0) {
                    writer.set(localX, localY, localZ, horizon.getState(localX + minX, localZ + minZ, height - localY, this.random));
                } else {
                    break;
                }
            }
        }
    }
}
