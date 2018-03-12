package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.terrarium.server.world.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.primer.GlobPrimer;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class SalineFloodedForestCover extends FloodedForestCover {
    public SalineFloodedForestCover() {
        super(CoverType.SALINE_FLOODED_FOREST);
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        this.iterate(point -> {
            int index = point.index;
            if (random.nextInt(3) == 0) {
                int y = this.heightBuffer[index];
                IBlockState state = primer.getBlockState(point.localX, y, point.localZ);
                if (state.getBlock() instanceof BlockDirt){
                    primer.setBlockState(point.localX, y + 1, point.localZ, TALL_GRASS);
                }
            }
        });
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        this.preventIntersection(1);

        int[] clearingLayer = this.sampleChunk(this.clearingSelector, x, z);
        int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, x, z);

        this.decorateScatterSample(random, x, z, this.range(random, 8, 10), point -> {
            if (clearingLayer[point.chunk.index] == 0) {
                int height = this.range(random, 5, 8) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
                BlockPos ground = point.pos.down();
                if (this.world.getBlockState(ground).getMaterial() == Material.SAND) {
                    this.world.setBlockState(ground, COARSE_DIRT);
                }
                if (random.nextInt(4) == 0) {
                    new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, false, false).generate(this.world, random, point.pos);
                } else {
                    new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, false, false).generate(this.world, random, point.pos);
                }
            }
        });

        this.stopIntersectionPrevention();
    }

    @Override
    public IBlockState getPrimaryCover() {
        return SAND;
    }

    @Override
    public boolean hasPodzol() {
        return false;
    }
}
