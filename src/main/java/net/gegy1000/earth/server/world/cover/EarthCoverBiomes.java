package net.gegy1000.earth.server.world.cover;

import net.gegy1000.earth.server.world.biome.BareBiome;
import net.gegy1000.earth.server.world.biome.SnowBiome;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class EarthCoverBiomes {
    public static final Biome BARE = new BareBiome();
    public static final Biome BEACH = Biomes.BEACH;
    public static final Biome BROADLEAF_EVERGREEN = Biomes.JUNGLE;
    public static final Biome CLOSED_BROADLEAF_DECIDUOUS = Biomes.FOREST;
    public static final Biome CLOSED_NEEDLELEAF_EVERGREEN = Biomes.TAIGA;
    public static final Biome CROPLAND_WITH_VEGETATION = Biomes.PLAINS;
    public static final Biome FLOODED_GRASSLAND = Biomes.SWAMP;
    public static final Biome FOREST_SHRUBLAND_WITH_GRASS = Biomes.PLAINS;
    public static final Biome FRESH_FLOODED_FOREST = Biomes.BAMBOO_JUNGLE;
    public static final Biome GRASSLAND = Biomes.PLAINS;
    public static final Biome GRASS_WITH_FOREST_SHRUBLAND = Biomes.PLAINS;
    public static final Biome IRRIGATED_CROPS = Biomes.PLAINS;
    public static final Biome MIXED_BROAD_NEEEDLELEAF = Biomes.FOREST;
    public static final Biome OPEN_BROADLEAF_DECIDUOUS = Biomes.FOREST;
    public static final Biome OPEN_NEEDLELEAF = Biomes.TAIGA;
    public static final Biome RAINFED_CROPS = Biomes.PLAINS;
    public static final Biome SALINE_FLOODED_FOREST = Biomes.BAMBOO_JUNGLE;
    public static final Biome SHRUBLAND = Biomes.PLAINS;
    public static final Biome SNOW = new SnowBiome();
    public static final Biome SPARSE_VEGETATION = new BareBiome();
    public static final Biome URBAN = Biomes.PLAINS;
    public static final Biome VEGETATION_WITH_CROPLAND = Biomes.PLAINS;
    public static final Biome WATER = Biomes.OCEAN;
    public static final Biome FLOWER_FIELD = Biomes.SUNFLOWER_PLAINS;
    public static final Biome SCREE = Biomes.GRAVELLY_MOUNTAINS;

    public enum Glob {
        IRRIGATED_CROPS(11, EarthCoverBiomes.IRRIGATED_CROPS),
        RAINFED_CROPS(14, EarthCoverBiomes.RAINFED_CROPS),
        CROPLAND_WITH_VEGETATION(20, EarthCoverBiomes.CROPLAND_WITH_VEGETATION),
        VEGETATION_WITH_CROPLAND(30, EarthCoverBiomes.VEGETATION_WITH_CROPLAND),
        BROADLEAF_EVERGREEN(40, EarthCoverBiomes.BROADLEAF_EVERGREEN),
        CLOSED_BROADLEAF_DECIDUOUS(50, EarthCoverBiomes.CLOSED_BROADLEAF_DECIDUOUS),
        OPEN_BROADLEAF_DECIDUOUS(60, EarthCoverBiomes.OPEN_BROADLEAF_DECIDUOUS),
        CLOSED_NEEDLELEAF_EVERGREEN(70, EarthCoverBiomes.CLOSED_NEEDLELEAF_EVERGREEN),
        OPEN_NEEDLELEAF(90, EarthCoverBiomes.OPEN_NEEDLELEAF),
        MIXED_BROAD_NEEDLELEAF(100, EarthCoverBiomes.MIXED_BROAD_NEEEDLELEAF),
        FOREST_SHRUBLAND_WITH_GRASS(110, EarthCoverBiomes.FOREST_SHRUBLAND_WITH_GRASS),
        GRASS_WITH_FOREST_SHRUBLAND(120, EarthCoverBiomes.GRASS_WITH_FOREST_SHRUBLAND),
        SHRUBLAND(130, EarthCoverBiomes.SHRUBLAND),
        GRASSLAND(140, EarthCoverBiomes.GRASSLAND),
        SPARSE_VEGETATION(150, EarthCoverBiomes.SPARSE_VEGETATION),
        FRESH_FLOODED_FOREST(160, EarthCoverBiomes.FRESH_FLOODED_FOREST),
        SALINE_FLOODED_FOREST(170, EarthCoverBiomes.SALINE_FLOODED_FOREST),
        FLOODED_GRASSLAND(180, EarthCoverBiomes.FLOODED_GRASSLAND),
        URBAN(190, EarthCoverBiomes.URBAN),
        BARE(200, EarthCoverBiomes.BARE),
        WATER(210, EarthCoverBiomes.WATER),
        SNOW(220, EarthCoverBiomes.SNOW),
        NO_DATA(0, EarthCoverBiomes.BARE);

        public static final Glob[] TYPES = Glob.values();
        public static final Glob[] GLOB_IDS = new Glob[256];

        private final byte id;
        private final Biome biome;

        Glob(int id, Biome biome) {
            this.id = (byte) (id & 0xFF);
            this.biome = biome;
        }

        public int getId() {
            return this.id;
        }

        public Biome getBiome() {
            return this.biome;
        }

        public static Glob get(int id) {
            Glob glob = GLOB_IDS[id & 0xFF];
            if (glob == null) {
                return NO_DATA;
            }
            return glob;
        }

        static {
            for (Glob glob : TYPES) {
                GLOB_IDS[glob.id & 0xFF] = glob;
            }
        }
    }
}
