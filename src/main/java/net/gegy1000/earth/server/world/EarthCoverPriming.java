package net.gegy1000.earth.server.world;

import com.google.common.collect.ImmutableList;
import net.gegy1000.earth.server.world.cover.carver.CoverCarver;

public final class EarthCoverPriming {
    public final ImmutableList<CoverCarver> carvers;

    private EarthCoverPriming(ImmutableList<CoverCarver> carvers) {
        this.carvers = carvers;
    }

    public static class Builder {
        private final ImmutableList.Builder<CoverCarver> carvers = ImmutableList.builder();

        public Builder addCarver(CoverCarver carver) {
            this.carvers.add(carver);
            return this;
        }

        public EarthCoverPriming build() {
            return new EarthCoverPriming(this.carvers.build());
        }
    }
}
