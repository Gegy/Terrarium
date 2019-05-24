package net.gegy1000.earth.server.world.cover;

public enum CoverId {
    NO_DATA(0, Cover.NONE),
    RAINFED_CROPLAND(10, Cover.RAINFED_CROPLAND),
    HERBACEOUS_COVER(11, Cover.HERBACEOUS_COVER),
    TREE_OR_SHRUB_COVER(12, Cover.TREE_OR_SHRUB_COVER),
    IRRIGATED_CROPLAND(20, Cover.IRRIGATED_CROPLAND),
    CROPLAND_WITH_VEGETATION(30, Cover.CROPLAND_WITH_VEGETATION),
    VEGETATION_WITH_CROPLAND(40, Cover.VEGETATION_WITH_CROPLAND),
    BROADLEAF_EVERGREEN(50, Cover.BROADLEAF_EVERGREEN),
    BROADLEAF_DECIDUOUS(60, Cover.BROADLEAF_DECIDUOUS),
    BROADLEAF_DECIDUOUS_CLOSED(61, Cover.BROADLEAF_DECIDUOUS_CLOSED),
    BROADLEAF_DECIDUOUS_OPEN(62, Cover.BROADLEAF_DECIDUOUS_OPEN),
    NEEDLEAF_EVERGREEN(70, Cover.NEEDLEAF_EVERGREEN),
    NEEDLEAF_EVERGREEN_CLOSED(71, Cover.NEEDLEAF_EVERGREEN_CLOSED),
    NEEDLEAF_EVERGREEN_OPEN(72, Cover.NEEDLEAF_EVERGREEN_OPEN),
    NEEDLEAF_DECIDUOUS(80, Cover.NEEDLEAF_DECIDUOUS),
    NEEDLEAF_DECIDUOUS_CLOSED(81, Cover.NEEDLEAF_DECIDUOUS_CLOSED),
    NEEDLEAF_DECIDUOUS_OPEN(82, Cover.NEEDLEAF_DECIDUOUS_OPEN),
    MIXED_LEAF_TYPE(90, Cover.MIXED_LEAF_TYPE),
    TREE_AND_SHRUB_WITH_HERBACEOUS_COVER(100, Cover.TREE_AND_SHRUB_WITH_HERBACEOUS_COVER),
    HERBACEOUS_COVER_WITH_TREE_AND_SHRUB(110, Cover.HERBACEOUS_COVER_WITH_TREE_AND_SHRUB),
    SHRUBLAND(120, Cover.SHRUBLAND),
    SHRUBLAND_EVERGREEN(121, Cover.SHRUBLAND_EVERGREEN),
    SHRUBLAND_DECIDUOUS(122, Cover.SHRUBLAND_DECIDUOUS),
    GRASSLAND(130, Cover.GRASSLAND),
    LICHENS_AND_MOSSES(140, Cover.LICHENS_AND_MOSSES),
    SPARSE_VEGETATION(150, Cover.SPARSE_VEGETATION),
    SPARSE_TREE(151, Cover.SPARSE_TREE),
    SPARSE_SHRUB(152, Cover.SPARSE_SHRUB),
    SPARSE_HERBACEOUS_COVER(153, Cover.SPARSE_HERBACEOUS_COVER),
    FRESH_FLOODED_FOREST(160, Cover.FRESH_FLOODED_FOREST),
    SALINE_FLOODED_FOREST(170, Cover.SALINE_FLOODED_FOREST),
    FLOODED_VEGETATION(180, Cover.FLOODED_VEGETATION),
    URBAN(190, Cover.URBAN),
    BARE(200, Cover.BARE),
    BARE_CONSOLIDATED(201, Cover.BARE_CONSOLIDATED),
    BARE_UNCONSOLIDATED(202, Cover.BARE_UNCONSOLIDATED),
    WATER(210, Cover.WATER),
    PERMANENT_SNOW(220, Cover.PERMANENT_SNOW);

    public static final CoverId[] TYPES = CoverId.values();
    private static final CoverId[] CLASSIFICATION_IDS = new CoverId[256];

    private final byte id;
    private final Cover cover;

    CoverId(int id, Cover cover) {
        this.id = (byte) (id & 0xFF);
        this.cover = cover;
    }

    public int getId() {
        return this.id;
    }

    public Cover getCover() {
        return this.cover;
    }

    public static CoverId get(int id) {
        CoverId classification = CLASSIFICATION_IDS[id & 0xFF];
        if (classification == null) {
            return NO_DATA;
        }
        return classification;
    }

    static {
        for (CoverId classification : TYPES) {
            CLASSIFICATION_IDS[classification.id & 0xFF] = classification;
        }
    }
}