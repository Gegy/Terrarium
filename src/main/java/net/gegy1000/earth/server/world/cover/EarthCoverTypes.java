package net.gegy1000.earth.server.world.cover;

import net.gegy1000.earth.server.world.cover.type.BareCover;
import net.gegy1000.earth.server.world.cover.type.BeachCover;
import net.gegy1000.earth.server.world.cover.type.BroadleafEvergreenCover;
import net.gegy1000.earth.server.world.cover.type.ClosedBroadleafDeciduousCover;
import net.gegy1000.earth.server.world.cover.type.ClosedNeedleleafEvergreenCover;
import net.gegy1000.earth.server.world.cover.type.CroplandWithVegetationCover;
import net.gegy1000.earth.server.world.cover.type.FloodedGrasslandCover;
import net.gegy1000.earth.server.world.cover.type.FlowerFieldCover;
import net.gegy1000.earth.server.world.cover.type.ForestShrublandWithGrassCover;
import net.gegy1000.earth.server.world.cover.type.FreshFloodedForestCover;
import net.gegy1000.earth.server.world.cover.type.GrassWithForestShrublandCover;
import net.gegy1000.earth.server.world.cover.type.GrasslandCover;
import net.gegy1000.earth.server.world.cover.type.IrrigatedCropsCover;
import net.gegy1000.earth.server.world.cover.type.MixedBroadNeedleleafCover;
import net.gegy1000.earth.server.world.cover.type.OpenBroadleafDeciduousCover;
import net.gegy1000.earth.server.world.cover.type.OpenNeedleleafCover;
import net.gegy1000.earth.server.world.cover.type.RainfedCropsCover;
import net.gegy1000.earth.server.world.cover.type.SalineFloodedForestCover;
import net.gegy1000.earth.server.world.cover.type.ScreeCover;
import net.gegy1000.earth.server.world.cover.type.ShrublandCover;
import net.gegy1000.earth.server.world.cover.type.SnowCover;
import net.gegy1000.earth.server.world.cover.type.SparseVegetationCover;
import net.gegy1000.earth.server.world.cover.type.UrbanCover;
import net.gegy1000.earth.server.world.cover.type.VegetationWithCroplandCover;
import net.gegy1000.earth.server.world.cover.type.WaterCover;
import net.gegy1000.terrarium.server.world.cover.CoverType;

import java.util.ArrayList;
import java.util.List;

public class EarthCoverTypes {
    public static final EarthCoverType BARE = new BareCover();
    public static final EarthCoverType BEACH = new BeachCover();
    public static final EarthCoverType BROADLEAF_EVERGREEN = new BroadleafEvergreenCover();
    public static final EarthCoverType CLOSED_BROADLEAF_DECIDUOUS = new ClosedBroadleafDeciduousCover();
    public static final EarthCoverType CLOSED_NEEDLELEAF_EVERGREEN = new ClosedNeedleleafEvergreenCover();
    public static final EarthCoverType CROPLAND_WITH_VEGETATION = new CroplandWithVegetationCover();
    public static final EarthCoverType FLOODED_GRASSLAND = new FloodedGrasslandCover();
    public static final EarthCoverType FOREST_SHRUBLAND_WITH_GRASS = new ForestShrublandWithGrassCover();
    public static final EarthCoverType FRESH_FLOODED_FOREST = new FreshFloodedForestCover();
    public static final EarthCoverType GRASSLAND = new GrasslandCover();
    public static final EarthCoverType GRASS_WITH_FOREST_SHRUBLAND = new GrassWithForestShrublandCover();
    public static final EarthCoverType IRRIGATED_CROPS = new IrrigatedCropsCover();
    public static final EarthCoverType MIXED_BROAD_NEEEDLELEAF = new MixedBroadNeedleleafCover();
    public static final EarthCoverType OPEN_BROADLEAF_DECIDUOUS = new OpenBroadleafDeciduousCover();
    public static final EarthCoverType OPEN_NEEDLELEAF = new OpenNeedleleafCover();
    public static final EarthCoverType RAINFED_CROPS = new RainfedCropsCover();
    public static final EarthCoverType SALINE_FLOODED_FOREST = new SalineFloodedForestCover();
    public static final EarthCoverType SHRUBLAND = new ShrublandCover();
    public static final EarthCoverType SNOW = new SnowCover();
    public static final EarthCoverType SPARSE_VEGETATION = new SparseVegetationCover();
    public static final EarthCoverType URBAN = new UrbanCover();
    public static final EarthCoverType VEGETATION_WITH_CROPLAND = new VegetationWithCroplandCover();
    public static final EarthCoverType WATER = new WaterCover();
    public static final EarthCoverType FLOWER_FIELD = new FlowerFieldCover();
    public static final EarthCoverType SCREE = new ScreeCover();

    public static final List<EarthCoverType> COVER_TYPES = new ArrayList<>();

    static {
        COVER_TYPES.add(BARE);
        COVER_TYPES.add(BEACH);
        COVER_TYPES.add(BROADLEAF_EVERGREEN);
        COVER_TYPES.add(CLOSED_BROADLEAF_DECIDUOUS);
        COVER_TYPES.add(CLOSED_NEEDLELEAF_EVERGREEN);
        COVER_TYPES.add(CROPLAND_WITH_VEGETATION);
        COVER_TYPES.add(FLOODED_GRASSLAND);
        COVER_TYPES.add(FOREST_SHRUBLAND_WITH_GRASS);
        COVER_TYPES.add(FRESH_FLOODED_FOREST);
        COVER_TYPES.add(GRASSLAND);
        COVER_TYPES.add(GRASS_WITH_FOREST_SHRUBLAND);
        COVER_TYPES.add(IRRIGATED_CROPS);
        COVER_TYPES.add(MIXED_BROAD_NEEEDLELEAF);
        COVER_TYPES.add(OPEN_BROADLEAF_DECIDUOUS);
        COVER_TYPES.add(OPEN_NEEDLELEAF);
        COVER_TYPES.add(RAINFED_CROPS);
        COVER_TYPES.add(SALINE_FLOODED_FOREST);
        COVER_TYPES.add(SHRUBLAND);
        COVER_TYPES.add(SNOW);
        COVER_TYPES.add(SPARSE_VEGETATION);
        COVER_TYPES.add(URBAN);
        COVER_TYPES.add(VEGETATION_WITH_CROPLAND);
        COVER_TYPES.add(WATER);
        COVER_TYPES.add(FLOWER_FIELD);
        COVER_TYPES.add(SCREE);
    }

    public enum Glob {
        IRRIGATED_CROPS(11, EarthCoverTypes.IRRIGATED_CROPS),
        RAINFED_CROPS(14, EarthCoverTypes.RAINFED_CROPS),
        CROPLAND_WITH_VEGETATION(20, EarthCoverTypes.CROPLAND_WITH_VEGETATION),
        VEGETATION_WITH_CROPLAND(30, EarthCoverTypes.VEGETATION_WITH_CROPLAND),
        BROADLEAF_EVERGREEN(40, EarthCoverTypes.BROADLEAF_EVERGREEN),
        CLOSED_BROADLEAF_DECIDUOUS(50, EarthCoverTypes.CLOSED_BROADLEAF_DECIDUOUS),
        OPEN_BROADLEAF_DECIDUOUS(60, EarthCoverTypes.OPEN_BROADLEAF_DECIDUOUS),
        CLOSED_NEEDLELEAF_EVERGREEN(70, EarthCoverTypes.CLOSED_NEEDLELEAF_EVERGREEN),
        OPEN_NEEDLELEAF(90, EarthCoverTypes.OPEN_NEEDLELEAF),
        MIXED_BROAD_NEEDLELEAF(100, EarthCoverTypes.MIXED_BROAD_NEEEDLELEAF),
        FOREST_SHRUBLAND_WITH_GRASS(110, EarthCoverTypes.FOREST_SHRUBLAND_WITH_GRASS),
        GRASS_WITH_FOREST_SHRUBLAND(120, EarthCoverTypes.GRASS_WITH_FOREST_SHRUBLAND),
        SHRUBLAND(130, EarthCoverTypes.SHRUBLAND),
        GRASSLAND(140, EarthCoverTypes.GRASSLAND),
        SPARSE_VEGETATION(150, EarthCoverTypes.SPARSE_VEGETATION),
        FRESH_FLOODED_FOREST(160, EarthCoverTypes.FRESH_FLOODED_FOREST),
        SALINE_FLOODED_FOREST(170, EarthCoverTypes.SALINE_FLOODED_FOREST),
        FLOODED_GRASSLAND(180, EarthCoverTypes.FLOODED_GRASSLAND),
        URBAN(190, EarthCoverTypes.URBAN),
        BARE(200, EarthCoverTypes.BARE),
        WATER(210, EarthCoverTypes.WATER),
        SNOW(220, EarthCoverTypes.SNOW),
        NO_DATA(0, EarthCoverTypes.BARE);

        public static final Glob[] TYPES = Glob.values();
        public static final Glob[] GLOB_IDS = new Glob[256];

        private final byte id;
        private final CoverType coverType;

        Glob(int id, CoverType coverType) {
            this.id = (byte) (id & 0xFF);
            this.coverType = coverType;
        }

        public int getId() {
            return this.id;
        }

        public CoverType getCoverType() {
            return this.coverType;
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
