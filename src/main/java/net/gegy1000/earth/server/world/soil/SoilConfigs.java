package net.gegy1000.earth.server.world.soil;

import static net.gegy1000.earth.server.world.soil.SoilHorizons.*;

public class SoilConfigs {
    public static final SoilConfig PERMANENT_SNOW = new UnarySoilConfig(SNOW_HORIZON);
    public static final SoilConfig UNDER_WATER = new UnarySoilConfig(GRAVEL_HORIZON);

    public static final SoilConfig NORMAL_SOIL = new BinarySoilConfig(GRASS_HORIZON, DIRT_HORIZON);
    public static final SoilConfig LOAMY_SOIL = new BinarySoilConfig(LOAMY_GRASS_HORIZON, LOAMY_DIRT_HORIZON);
    public static final SoilConfig SANDY_SOIL = new BinarySoilConfig(SANDY_GRASS_HORIZON, SANDY_DIRT_HORIZON);
    public static final SoilConfig SILTY_SOIL = new BinarySoilConfig(SILTY_GRASS_HORIZON, SILTY_DIRT_HORIZON);

    public static final SoilConfig SANDY_DIRT = new UnarySoilConfig(SANDY_DIRT_HORIZON);

    public static final SoilConfig SANDY = new UnarySoilConfig(SAND_HORIZON);
    public static final SoilConfig GRAVELLY = new UnarySoilConfig(GRAVEL_HORIZON);
    public static final SoilConfig CLAY = new UnarySoilConfig(CLAY_HORIZON);
    public static final SoilConfig BLACK_SOIL = new UnarySoilConfig(BLACK_SOIL_HORIZON);
    public static final SoilConfig COARSE_AND_CLAY = new BinarySoilConfig(GRASS_HORIZON, COARSE_CLAY_HORIZON);

    public static final SoilConfig ACRISOL = new UnarySoilConfig(ACRISOL_HORIZON);
    public static final SoilConfig ALBELUVISOL = new TernarySoilConfig(LOAMY_GRASS_HORIZON, CLAY_HORIZON, SILTY_DIRT_HORIZON);
    public static final SoilConfig LEPTOSOL = new BinarySoilConfig(GRAVEL_HORIZON, STONE_HORIZON);
    public static final SoilConfig LIXISOL = new BinarySoilConfig(LIXISOL_GRASS_HORIZON, LIXISOL_DIRT_HORIZON);
    public static final SoilConfig LUVISOL = new BinarySoilConfig(GRASS_HORIZON, LUVISOL_DIRT_HORIZON);
    public static final SoilConfig NITISOL = new UnarySoilConfig(NITISOL_HORIZON);
    public static final SoilConfig PLINTHOSOL = new BinarySoilConfig(GRASS_HORIZON, PLINTHOSOL_SUBSOIL);
    public static final SoilConfig PODZOL = new BinarySoilConfig(PODZOL_HORIZON, DIRT_HORIZON);
    public static final SoilConfig REGOSOL = new TernarySoilConfig(SANDY_GRASS_HORIZON, SANDY_DIRT_HORIZON, STONE_HORIZON);
}
