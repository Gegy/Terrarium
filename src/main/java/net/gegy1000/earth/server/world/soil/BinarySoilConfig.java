package net.gegy1000.earth.server.world.soil;

import net.gegy1000.earth.server.world.soil.horizon.SoilHorizonConfig;

public class BinarySoilConfig implements SoilConfig {
    private final SoilHorizonConfig topsoil;
    private final SoilHorizonConfig subsoil;

    public BinarySoilConfig(SoilHorizonConfig topsoil, SoilHorizonConfig subsoil) {
        this.topsoil = topsoil;
        this.subsoil = subsoil;
    }

    @Override
    public SoilHorizonConfig getHorizon(int depth) {
        if (depth == 0) {
            return this.topsoil;
        } else {
            return this.subsoil;
        }
    }
}
