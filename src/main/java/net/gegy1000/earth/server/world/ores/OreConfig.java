package net.gegy1000.earth.server.world.ores;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.server.world.feature.SingleOreGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;

public final class OreConfig {
    private final WorldGenerator generator;
    private final OreDistribution distribution;

    private OreConfig(WorldGenerator generator, OreDistribution distribution) {
        this.generator = generator;
        this.distribution = distribution;
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

    public static class Builder {
        private IBlockState ore;
        private OreDistribution distribution;
        private int size = 1;

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

        public OreConfig build() {
            Preconditions.checkNotNull(this.ore, "ore state not set");
            Preconditions.checkNotNull(this.distribution, "distribution not set");

            WorldGenerator generator = this.buildGenerator();
            return new OreConfig(generator, this.distribution);
        }

        private WorldGenerator buildGenerator() {
            if (this.size == 1) return new SingleOreGenerator(this.ore);
            return new WorldGenMinable(this.ore, this.size);
        }
    }
}
