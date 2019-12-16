package net.gegy1000.earth.server.world.feature;

import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public final class SingleOreGenerator extends WorldGenerator {
    private final IBlockState ore;
    private final Predicate<IBlockState> targetPredicate;

    public SingleOreGenerator(IBlockState ore, Predicate<IBlockState> targetPredicate) {
        this.ore = ore;
        this.targetPredicate = targetPredicate;
    }

    public SingleOreGenerator(IBlockState ore) {
        this(ore, BlockMatcher.forBlock(Blocks.STONE));
    }

    @Override
    public boolean generate(World world, Random random, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock().isReplaceableOreGen(state, world, pos, this.targetPredicate)) {
            world.setBlockState(pos, this.ore, 16 | 2);
        }
        return true;
    }
}
