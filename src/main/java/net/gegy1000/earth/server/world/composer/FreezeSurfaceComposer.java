package net.gegy1000.earth.server.world.composer;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UByteRaster;
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

    private final UByteRaster.Sampler slopeSampler;

    private final SpatialRandom random;

    public FreezeSurfaceComposer(World world, DataKey<UByteRaster> slopeKey) {
        this.slopeSampler = UByteRaster.sampler(slopeKey);

        this.random = new SpatialRandom(world, SCATTER_SEED);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        World world = writer.getGlobal();

        int globalX = pos.getCenterX();
        int globalZ = pos.getCenterZ();

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        DataView view = DataView.square(globalX, globalZ, 16);

        UByteRaster slopeRaster = this.slopeSampler.sample(dataCache, view);

        this.random.setSeed(pos.getX(), pos.getZ());

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                mutablePos.setPos(globalX + localX, 0, globalZ + localZ);

                Biome biome = world.getBiome(mutablePos);
                float temperature = biome.getTemperature(mutablePos);
                if (temperature >= 0.15F) continue;

                BlockPos surfacePos = writer.getSurface(mutablePos);
                if (surfacePos == null) continue;

                BlockPos groundPos = surfacePos.down();
                if (this.canBeFrozen(world, groundPos)) {
                    writer.set(groundPos, Blocks.ICE.getDefaultState());
                }

                int slope = slopeRaster.get(localX, localZ);
                if (slope < MAX_SLOPE && this.canBeSnowedOn(world, surfacePos)) {
                    writer.set(surfacePos, Blocks.SNOW_LAYER.getDefaultState());
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
