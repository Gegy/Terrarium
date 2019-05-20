package net.gegy1000.earth.server.world.ecology;

public interface GrowthIndicator {
    static GrowthIndicator anywhere() {
        return abiotic -> 1.0;
    }

    double test(AbioticComponents abiotic);
}
