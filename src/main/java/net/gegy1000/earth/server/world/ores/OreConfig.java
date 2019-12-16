package net.gegy1000.earth.server.world.ores;

import com.google.common.base.Preconditions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.feature.WorldGenMinable;

public final class OreConfig {
    private final WorldGenMinable generator;
    private final OreDistribution distribution;

    private OreConfig(WorldGenMinable generator, OreDistribution distribution) {
        this.generator = generator;
        this.distribution = distribution;
    }

    public static Builder builder() {
        return new Builder();
    }

    public WorldGenMinable getGenerator() {
        return this.generator;
    }

    public OreDistribution getDistribution() {
        return this.distribution;
    }

    public static class Builder {
        private IBlockState ore;
        private OreDistribution distribution;
        private int veinSize = 1;

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

        public Builder veinSize(int veinSize) {
            this.veinSize = veinSize;
            return this;
        }

        public OreConfig build() {
            Preconditions.checkNotNull(this.ore, "ore state not set");
            Preconditions.checkNotNull(this.distribution, "distribution not set");
            WorldGenMinable generator = new WorldGenMinable(this.ore, this.veinSize);
            return new OreConfig(generator, this.distribution);
        }
    }
}
