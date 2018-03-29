package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class PlaceholderCover implements CoverType<CoverGenerationContext> {
    @Override
    public CoverSurfaceGenerator<CoverGenerationContext> createSurfaceGenerator(CoverGenerationContext context) {
        return new Surface(context, this);
    }

    @Override
    public CoverDecorationGenerator<CoverGenerationContext> createDecorationGenerator(CoverGenerationContext context) {
        return new CoverDecorationGenerator.Empty<>(context, this);
    }

    @Override
    public Biome getBiome(CoverGenerationContext context, int x, int z) {
        return Biomes.DEFAULT;
    }

    @Override
    public Class<CoverGenerationContext> getRequiredContext() {
        return CoverGenerationContext.class;
    }

    private static class Surface extends CoverSurfaceGenerator<CoverGenerationContext> {
        private Surface(CoverGenerationContext context, CoverType<CoverGenerationContext> coverType) {
            super(context, coverType);
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            this.coverBlock(coverBlockBuffer, Blocks.QUARTZ_BLOCK.getDefaultState());
        }

        @Override
        public void populateBlockFiller(Random random, int originX, int originZ, IBlockState[] fillerBlockBuffer) {
            this.coverBlock(fillerBlockBuffer, Blocks.QUARTZ_BLOCK.getDefaultState());
        }
    }
}
