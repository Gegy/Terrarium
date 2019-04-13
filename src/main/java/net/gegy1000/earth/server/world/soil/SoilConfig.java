package net.gegy1000.earth.server.world.soil;

import net.gegy1000.earth.server.world.soil.horizon.SoilHorizonConfig;

public interface SoilConfig {
    SoilHorizonConfig getHorizon(int depth);
}
