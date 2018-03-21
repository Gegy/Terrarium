package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class BeachCover implements CoverType {
    @Override
    public CoverSurfaceGenerator createSurfaceGenerator(CoverGenerationContext context) {
        IBlockState state = Blocks.SAND.getDefaultState();
        return new CoverSurfaceGenerator.Static(context, this, state, state);
    }

    @Override
    public CoverDecorationGenerator createDecorationGenerator(CoverGenerationContext context) {
        return new Decoration(context, this);
    }

    @Override
    public Biome getBiome(int x, int z) {
        return Biomes.BEACH;
    }

    private static class Decoration extends CoverDecorationGenerator {
        private Decoration(CoverGenerationContext context, CoverType coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(int originX, int originZ, Random random) {
            // TODO: Check tropical
            World world = this.context.getWorld();
            this.decorateScatter(random, originX, originZ, this.range(random, 0, 2), (pos, localX, localZ) -> {
                int height = this.range(random, 6, 7);
                new GenerousTreeGenerator(false, height, JUNGLE_LOG, JUNGLE_LEAF, true, true).generate(world, random, pos);
            });
        }
    }
}
