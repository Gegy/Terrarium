package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class FreezeSurfaceComposer implements DecorationComposer {
    private static final long SCATTER_SEED = 6193809942152828777L;

    private static final int MAX_SLOPE = 60;

    private final ShortRaster.Sampler heightSampler = ShortRaster.sampler(EarthData.TERRAIN_HEIGHT);
    private final UByteRaster.Sampler slopeSampler = UByteRaster.sampler(EarthData.SLOPE);

    private final SpatialRandom random;

    public FreezeSurfaceComposer(World world) {
        this.random = new SpatialRandom(world, SCATTER_SEED);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        World world = writer.getGlobal();

        int minX = pos.getCenterX();
        int minZ = pos.getCenterZ();

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        DataView view = DataView.square(minX, minZ, 16);

        ShortRaster heightRaster = this.heightSampler.sample(dataCache, view);
        UByteRaster slopeRaster = this.slopeSampler.sample(dataCache, view);

        this.random.setSeed(pos.getX(), pos.getZ());

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                mutablePos.setPos(
                        minX + x + this.random.nextInt(3) - this.random.nextInt(3),
                        0,
                        minZ + z + this.random.nextInt(3) - this.random.nextInt(3)
                );

                Biome biome = world.getBiome(mutablePos);
                float temperature = biome.getTemperature(mutablePos);
                if (temperature >= 0.15F) continue;

                mutablePos.setPos(minX + x, 0, minZ + z);

                if (!writer.getSurfaceMut(mutablePos)) continue;

                short terrainSurface = heightRaster.get(x, z);
                if (mutablePos.getY() < terrainSurface) {
                    continue;
                }

                BlockPos groundPos = mutablePos.down();
                if (this.canBeFrozen(world, groundPos)) {
                    writer.set(groundPos, Blocks.ICE.getDefaultState());
                }

                int slope = slopeRaster.get(x, z);
                if (slope < MAX_SLOPE && this.canBeSnowedOn(world, mutablePos)) {
                    writer.set(mutablePos, Blocks.SNOW_LAYER.getDefaultState());
                }
            }
        }
    }

    private boolean canBeSnowedOn(World world, BlockPos surfacePos) {
        IBlockState surfaceState = world.getBlockState(surfacePos);
        Block surfaceBlock = surfaceState.getBlock();
        return surfaceBlock.isAir(surfaceState, world, surfacePos) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, surfacePos);
    }

    private boolean canBeFrozen(World world, BlockPos groundPos) {
        IBlockState groundState = world.getBlockState(groundPos);
        Block groundBlock = groundState.getBlock();
        return (groundBlock == Blocks.WATER || groundBlock == Blocks.FLOWING_WATER) && groundState.getValue(BlockLiquid.LEVEL) == 0;
    }
}
