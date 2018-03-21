package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class SnowCover implements CoverType {
    private static final IBlockState SNOW = Blocks.SNOW.getDefaultState();
    private static final IBlockState ICE = Blocks.PACKED_ICE.getDefaultState();

    @Override
    public CoverSurfaceGenerator createSurfaceGenerator(CoverGenerationContext context) {
        return new Surface(context, this);
    }

    @Override
    public CoverDecorationGenerator createDecorationGenerator(CoverGenerationContext context) {
        return new CoverDecorationGenerator.Empty(context, this);
    }

    @Override
    public Biome getBiome(int x, int z) {
        return Biomes.ICE_PLAINS;
    }

    private static class Surface extends CoverSurfaceGenerator {
        private Surface(CoverGenerationContext context, CoverType coverType) {
            super(context, coverType);
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            ByteRasterTileAccess slopeRaster = this.context.getSlopeRaster();
            this.iterateChunk((localX, localZ) -> {
                int slope = slopeRaster.getUnsigned(localX, localZ);
                coverBlockBuffer[localX + localZ * 16] = slope >= EXTREME_CLIFF_SLOPE ? ICE : SNOW;
            });
        }

        @Override
        public void populateBlockFiller(Random random, int originX, int originZ, IBlockState[] fillerBlockBuffer) {
            this.populateBlockCover(random, originX, originZ, fillerBlockBuffer);
        }
    }
}
