package net.gegy1000.earth.server.world.ecology.vegetation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;

public final class Vegetation {
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

    public static class Builder {
        private final ImmutableList.Builder<GrowthIndicator> growthIndicators = ImmutableList.builder();
        private VegetationGenerator generator;

        Builder() {
        }

        public Builder withGrowthIndicator(GrowthIndicator indicator) {
            this.growthIndicators.add(indicator);
            return this;
        }

        public Builder withGenerator(VegetationGenerator generator) {
            this.generator = generator;
            return this;
        }

        public Vegetation build() {
            Preconditions.checkNotNull(this.generator, "generator cannot be null");
            ImmutableList<GrowthIndicator> growthIndicators = this.growthIndicators.build();

            GrowthIndicator indicator = GrowthIndicator.anywhere();
            if (!growthIndicators.isEmpty()) {
                indicator = abiotic -> {
                    double value = 1.0;
                    for (GrowthIndicator i : growthIndicators) {
                        value *= i.test(abiotic);
                    }
                    return value;
                };
            }

            return new Vegetation(indicator, this.generator);
        }
    }
}
