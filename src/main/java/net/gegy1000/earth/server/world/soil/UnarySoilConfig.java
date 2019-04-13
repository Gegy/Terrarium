package net.gegy1000.earth.server.world.soil;

import net.gegy1000.earth.server.world.soil.horizon.SoilHorizonConfig;

public class UnarySoilConfig implements SoilConfig {
    private final SoilHorizonConfig soil;

    public UnarySoilConfig(SoilHorizonConfig soil) {
        this.soil = soil;
    }

    @Override
    public SoilHorizonConfig getHorizon(int depth) {
        return this.soil;
    }
}
