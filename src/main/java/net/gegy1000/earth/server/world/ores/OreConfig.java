package net.gegy1000.earth.server.world.ores;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public final class OreConfig {
    private final WorldGenerator generator;
    private final OreDistribution distribution;
    private final Selector selector;

    private OreConfig(WorldGenerator generator, OreDistribution distribution, Selector selector) {
        this.generator = generator;
        this.distribution = distribution;
        this.selector = selector;
    }

    public static Builder builder() {
        return new Builder();
    }

    public WorldGenerator getGenerator() {
        return this.generator;
    }

    public OreDistribution getDistribution() {
        return this.distribution;
    }

    public Selector getSelector() {
        return this.selector;
    }

    public static class Builder {
        private IBlockState ore;
        private OreDistribution distribution;
        private int size = 1;
        private Selector selector;

        Builder() {
        }

        public Builder ore(IBlockState state) {
            this.ore = state;
            return this;
        }

        public Builder distribution(OreDistribution distribution) {
            this.distribution = distribution;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder select(Selector selector) {
            this.selector = selector;
            return this;
        }

        public OreConfig build() {
            Preconditions.checkNotNull(this.ore, "ore state not set");
            Preconditions.checkNotNull(this.distribution, "distribution not set");

            WorldGenerator generator = this.buildGenerator();
            Selector selector = this.selector != null ? this.selector : (cache, x, z) -> true;

            return new OreConfig(generator, this.distribution, selector);
        }

        private WorldGenerator buildGenerator() {
            if (this.size == 1) return new SingleOreGenerator(this.ore);
            return new WorldGenMinable(this.ore, this.size);
        }
    }

    public interface Selector {
        boolean shouldGenerateAt(ColumnDataCache dataCache, int x, int z);
    }

    static class SingleOreGenerator extends WorldGenerator {
        private final IBlockState ore;
        private final Predicate<IBlockState> targetPredicate;

        SingleOreGenerator(IBlockState ore, Predicate<IBlockState> targetPredicate) {
            this.ore = ore;
            this.targetPredicate = targetPredicate;
        }

        SingleOreGenerator(IBlockState ore) {
            this(ore, BlockMatcher.forBlock(Blocks.STONE));
        }

        @Override
        public boolean generate(World world, Random random, BlockPos pos) {
            pos = pos.add(8, 0, 8);
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock().isReplaceableOreGen(state, world, pos, this.targetPredicate)) {
                world.setBlockState(pos, this.ore, 16 | 2);
            }
            return true;
        }
    }
}
