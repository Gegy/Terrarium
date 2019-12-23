package net.gegy1000.earth.server.world.ores;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.server.world.feature.SingleOreGenerator;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;

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
}
