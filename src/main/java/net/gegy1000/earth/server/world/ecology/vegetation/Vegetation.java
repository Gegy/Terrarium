package net.gegy1000.earth.server.world.ecology.vegetation;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;

public final class Vegetation {
    private static final Vegetation EMPTY = new Vegetation(GrowthIndicator.relaxed(), (world, random, pos) -> {});

    private final GrowthIndicator growthIndicator;
    private final VegetationGenerator generator;

    private Vegetation(GrowthIndicator growthIndicator, VegetationGenerator generator) {
        this.growthIndicator = growthIndicator;
        this.generator = generator;
    }

    public GrowthIndicator getGrowthIndicator() {
        return this.growthIndicator;
    }

    public VegetationGenerator getGenerator() {
        return this.generator;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Vegetation empty() {
        return EMPTY;
    }

    public static class Builder {
        private GrowthIndicator growthIndicator = GrowthIndicator.relaxed();
        private VegetationGenerator generator;

        Builder() {
        }

        public Builder growthIndicator(GrowthIndicator indicator) {
            this.growthIndicator = indicator;
            return this;
        }

        public Builder generator(VegetationGenerator generator) {
            this.generator = generator;
            return this;
        }

        public Vegetation build() {
            Preconditions.checkNotNull(this.generator, "generator cannot be null");
            return new Vegetation(this.growthIndicator, this.generator);
        }
    }
}
