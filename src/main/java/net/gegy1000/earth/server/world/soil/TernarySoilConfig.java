package net.gegy1000.earth.server.world.soil;

import net.gegy1000.earth.server.world.soil.horizon.SoilHorizonConfig;

public class TernarySoilConfig implements SoilConfig {
    private final SoilHorizonConfig topsoil;
    private final SoilHorizonConfig subsoilUpper;
    private final SoilHorizonConfig subsoilLower;

    public TernarySoilConfig(SoilHorizonConfig topsoil, SoilHorizonConfig subsoilUpper, SoilHorizonConfig subsoilLower) {
        this.topsoil = topsoil;
        this.subsoilUpper = subsoilUpper;
        this.subsoilLower = subsoilLower;
    }

    @Override
    public SoilHorizonConfig getHorizon(int depth) {
        if (depth == 0) {
            return this.topsoil;
        } else if (depth == 1) {
            return this.subsoilUpper;
        } else {
            return this.subsoilLower;
        }
    }
}
