package net.gegy1000.earth.server.world.cover;

import net.gegy1000.terrarium.server.world.cover.CoverType;

public enum CoverClassification {
    // TODO: These are all wrong now
    NO_DATA(0, EarthCoverTypes.BARE),
    RAINFED_CROPLAND(10, EarthCoverTypes.RAINFED_CROPS),
    HERBACEOUS_COVER(11, EarthCoverTypes.RAINFED_CROPS),
    TREE_OR_SHRUB_COVER(12, EarthCoverTypes.GRASS_WITH_FOREST_SHRUBLAND),
    IRRIGATED_CROPLAND(20, EarthCoverTypes.IRRIGATED_CROPS),
    CROPLAND_WITH_VEGETATION(30, EarthCoverTypes.CROPLAND_WITH_VEGETATION),
    VEGETATION_WITH_CROPLAND(40, EarthCoverTypes.VEGETATION_WITH_CROPLAND),
    BROADLEAF_EVERGREEN(50, EarthCoverTypes.BROADLEAF_EVERGREEN),
    BROADLEAF_DECIDUOUS(60, EarthCoverTypes.OPEN_BROADLEAF_DECIDUOUS),
    BROADLEAF_DECIDUOUS_CLOSED(61, EarthCoverTypes.CLOSED_BROADLEAF_DECIDUOUS),
    BROADLEAF_DECIDUOUS_OPEN(62, EarthCoverTypes.OPEN_BROADLEAF_DECIDUOUS),
    NEEDLEAF_EVERGREEN(70, EarthCoverTypes.CLOSED_NEEDLELEAF_EVERGREEN),
    NEEDLEAF_EVERGREEN_CLOSED(71, EarthCoverTypes.CLOSED_NEEDLELEAF_EVERGREEN),
    NEEDLEAF_EVERGREEN_OPEN(72, EarthCoverTypes.OPEN_NEEDLELEAF),
    NEEDLEAF_DECIDUOUS(80, EarthCoverTypes.OPEN_NEEDLELEAF),
    NEEDLEAF_DECIDUOUS_CLOSED(81, EarthCoverTypes.CLOSED_NEEDLELEAF_EVERGREEN),
    NEEDLEAF_DECIDUOUS_OPEN(82, EarthCoverTypes.OPEN_NEEDLELEAF),
    MIXED_BROAD_NEEDLELEAF(90, EarthCoverTypes.MIXED_BROAD_NEEEDLELEAF),
    TREE_AND_SHRUB_WITH_HERBACEOUS_COVER(100, EarthCoverTypes.FOREST_SHRUBLAND_WITH_GRASS),
    HERBACEOUS_COVER_WITH_TREE_AND_SHRUB(110, EarthCoverTypes.GRASS_WITH_FOREST_SHRUBLAND),
    SHRUBLAND(120, EarthCoverTypes.SHRUBLAND),
    SHRUBLAND_EVERGREEN(121, EarthCoverTypes.SHRUBLAND),
    SHRUBLAND_DECIDUOUS(122, EarthCoverTypes.SHRUBLAND),
    GRASSLAND(130, EarthCoverTypes.GRASSLAND),
    LICHENS_AND_MOSSES(140, EarthCoverTypes.SHRUBLAND),
    SPARSE_VEGETATION(150, EarthCoverTypes.SPARSE_VEGETATION),
    SPARSE_TREE(151, EarthCoverTypes.SPARSE_VEGETATION),
    SPARSE_SHRUB(152, EarthCoverTypes.SPARSE_VEGETATION),
    SPARSE_HERBACEOUS_COVER(153, EarthCoverTypes.SPARSE_VEGETATION),
    FRESH_FLOODED_FOREST(160, EarthCoverTypes.FRESH_FLOODED_FOREST),
    SALINE_FLOODED_FOREST(170, EarthCoverTypes.SALINE_FLOODED_FOREST),
    FLOODED_VEGETATION(180, EarthCoverTypes.FLOODED_GRASSLAND),
    URBAN(190, EarthCoverTypes.URBAN),
    BARE(200, EarthCoverTypes.BARE),
    BARE_CONSOLIDATED(201, EarthCoverTypes.BARE),
    BARE_UNCONSOLIDATED(202, EarthCoverTypes.BARE),
    WATER(210, EarthCoverTypes.WATER),
    PERMANENT_SNOW(220, EarthCoverTypes.SNOW);

    public static final CoverClassification[] TYPES = CoverClassification.values();
    public static final CoverClassification[] CLASSIFICATION_IDS = new CoverClassification[256];

    private final byte id;
    private final CoverType coverType;

    CoverClassification(int id, CoverType coverType) {
        this.id = (byte) (id & 0xFF);
        this.coverType = coverType;
    }

    public int getId() {
        return this.id;
    }

    public CoverType getCoverType() {
        return this.coverType;
    }

    public static CoverClassification get(int id) {
        CoverClassification classification = CLASSIFICATION_IDS[id & 0xFF];
        if (classification == null) {
            return NO_DATA;
        }
        return classification;
    }

    static {
        for (CoverClassification classification : TYPES) {
            CLASSIFICATION_IDS[classification.id & 0xFF] = classification;
        }
    }
}
