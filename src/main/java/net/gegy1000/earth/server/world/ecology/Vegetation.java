package net.gegy1000.earth.server.world.ecology;

import com.google.common.base.Preconditions;

public final class Vegetation {
    private final Habitat habitat;
    private final VegetationGenerator generator;

    private Vegetation(Habitat habitat, VegetationGenerator generator) {
        this.habitat = habitat;
        this.generator = generator;
    }

    public Habitat getHabitat() {
        return this.habitat;
    }

    public VegetationGenerator getGenerator() {
        return this.generator;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Habitat habitat;
        private VegetationGenerator generator;

        Builder() {
        }

        public Builder withHabitat(Habitat habitat) {
            this.habitat = habitat;
            return this;
        }

        public Builder withGenerator(VegetationGenerator generator) {
            this.generator = generator;
            return this;
        }

        public Vegetation build() {
            Preconditions.checkNotNull(this.habitat, "predicate cannot be null");
            Preconditions.checkNotNull(this.generator, "generator cannot be null");

            return new Vegetation(this.habitat, this.generator);
        }
    }
}
