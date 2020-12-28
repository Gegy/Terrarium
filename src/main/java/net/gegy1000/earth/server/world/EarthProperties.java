package net.gegy1000.earth.server.world;

import net.gegy1000.terrarium.server.world.generator.customization.SimplePropertySchema;
import net.gegy1000.terrarium.server.world.generator.customization.property.BooleanKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.NumberKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;

public final class EarthProperties {
    public static final PropertyKey<Number> SPAWN_LATITUDE = new NumberKey("spawn_latitude");
    public static final PropertyKey<Number> SPAWN_LONGITUDE = new NumberKey("spawn_longitude");
    public static final PropertyKey<Boolean> ADD_TREES = new BooleanKey("add_trees");
    public static final PropertyKey<Boolean> ADD_GRASS = new BooleanKey("add_grass");
    public static final PropertyKey<Boolean> ADD_FLOWERS = new BooleanKey("add_flowers");
    public static final PropertyKey<Boolean> ADD_CACTI = new BooleanKey("add_cacti");
    public static final PropertyKey<Boolean> ADD_SUGAR_CANE = new BooleanKey("add_sugar_cane");
    public static final PropertyKey<Boolean> ADD_GOURDS = new BooleanKey("add_gourds");
    public static final PropertyKey<Number> WORLD_SCALE = new NumberKey("world_scale");
    public static final PropertyKey<Number> TERRESTRIAL_HEIGHT_SCALE = new NumberKey("terrestrial_height_scale");
    public static final PropertyKey<Number> OCEANIC_HEIGHT_SCALE = new NumberKey("oceanic_height_scale");
    public static final PropertyKey<Number> HEIGHT_OFFSET = new NumberKey("height_offset");

    public static final PropertyKey<Boolean> CAVE_GENERATION = new BooleanKey("cave_generation");
    public static final PropertyKey<Boolean> RAVINE_GENERATION = new BooleanKey("ravine_generation");
    public static final PropertyKey<Boolean> ORE_GENERATION = new BooleanKey("ore_generation");
    public static final PropertyKey<Boolean> ADD_LAVA_POOLS = new BooleanKey("add_lava_pools");

    public static final PropertyKey<Boolean> ADD_STRONGHOLDS = new BooleanKey("add_strongholds");
    public static final PropertyKey<Boolean> ADD_VILLAGES = new BooleanKey("add_villages");
    public static final PropertyKey<Boolean> ADD_MINESHAFTS = new BooleanKey("add_mineshafts");
    public static final PropertyKey<Boolean> ADD_TEMPLES = new BooleanKey("add_temples");
    public static final PropertyKey<Boolean> ADD_OCEAN_MONUMENTS = new BooleanKey("add_ocean_monuments");
    public static final PropertyKey<Boolean> ADD_WOODLAND_MANSIONS = new BooleanKey("add_woodland_mansions");

    public static final PropertyKey<Boolean> COMPATIBILITY_MODE = new BooleanKey("compatibility_mode");
    public static final PropertyKey<Boolean> BOP_INTEGRATION = new BooleanKey("bop_integration");

    public static final PropertyKey<?>[] PROPERTIES = new PropertyKey<?>[] {
            SPAWN_LATITUDE, SPAWN_LONGITUDE,
            WORLD_SCALE, TERRESTRIAL_HEIGHT_SCALE, OCEANIC_HEIGHT_SCALE,
            HEIGHT_OFFSET,
            ADD_TREES, ADD_GRASS, ADD_FLOWERS, ADD_CACTI, ADD_SUGAR_CANE, ADD_GOURDS,
            CAVE_GENERATION, RAVINE_GENERATION, ORE_GENERATION, ADD_LAVA_POOLS,
            ADD_STRONGHOLDS, ADD_VILLAGES, ADD_MINESHAFTS, ADD_TEMPLES, ADD_OCEAN_MONUMENTS, ADD_WOODLAND_MANSIONS,
            COMPATIBILITY_MODE, BOP_INTEGRATION,
    };

    public static SimplePropertySchema SCHEMA = SimplePropertySchema.builder()
            .withProperties(PROPERTIES)
            .build();

    public static final class Legacy {
        public static final PropertyKey<Number> SPAWN_LATITUDE = new NumberKey("spawn_latitude");
        public static final PropertyKey<Number> SPAWN_LONGITUDE = new NumberKey("spawn_longitude");

        public static final PropertyKey<Boolean> ENABLE_DECORATION = new BooleanKey("enable_decoration");
        public static final PropertyKey<Boolean> ENABLE_DEFAULT_DECORATION = new BooleanKey("enable_default_decoration");

        public static final PropertyKey<Number> WORLD_SCALE = new NumberKey("world_scale");
        public static final PropertyKey<Number> HEIGHT_SCALE = new NumberKey("height_scale");
        public static final PropertyKey<Number> NOISE_SCALE = new NumberKey("noise_scale");
        public static final PropertyKey<Number> HEIGHT_ORIGIN = new NumberKey("height_origin");
        public static final PropertyKey<Number> OCEAN_DEPTH = new NumberKey("ocean_depth");
        public static final PropertyKey<Number> BEACH_SIZE = new NumberKey("beach_size");

        public static final PropertyKey<Boolean> ENABLE_BUILDINGS = new BooleanKey("enable_buildings");
        public static final PropertyKey<Boolean> ENABLE_STREETS = new BooleanKey("enable_streets");

        public static final PropertyKey<Boolean> ENABLE_DEFAULT_FEATURES = new BooleanKey("enable_default_features");
        public static final PropertyKey<Boolean> ENABLE_CAVE_GENERATION = new BooleanKey("enable_cave_generation");
        public static final PropertyKey<Boolean> ENABLE_RESOURCE_GENERATION = new BooleanKey("enable_resource_generation");
        public static final PropertyKey<Boolean> ENABLE_LAKE_GENERATION = new BooleanKey("enable_lake_generation");
        public static final PropertyKey<Boolean> ENABLE_LAVA_GENERATION = new BooleanKey("enable_lava_generation");
        public static final PropertyKey<Boolean> ENABLE_MOD_GENERATION = new BooleanKey("enable_mod_generation");

        public static final PropertyKey<?>[] PROPERTIES = new PropertyKey<?>[] {
                SPAWN_LATITUDE, SPAWN_LONGITUDE,
                ENABLE_DECORATION, ENABLE_DEFAULT_DECORATION,
                WORLD_SCALE, HEIGHT_SCALE,
                HEIGHT_ORIGIN, NOISE_SCALE,
                OCEAN_DEPTH, BEACH_SIZE,
                ENABLE_BUILDINGS, ENABLE_STREETS,
                ENABLE_DEFAULT_FEATURES, ENABLE_MOD_GENERATION,
                ENABLE_CAVE_GENERATION, ENABLE_RESOURCE_GENERATION,
                ENABLE_LAKE_GENERATION, ENABLE_LAVA_GENERATION,
        };

        public static SimplePropertySchema SCHEMA = SimplePropertySchema.builder()
                .withProperties(PROPERTIES)
                .build();
    }
}
