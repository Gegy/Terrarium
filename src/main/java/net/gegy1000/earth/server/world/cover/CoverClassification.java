package net.gegy1000.earth.server.world.cover;

public enum CoverClassification {
    NO_DATA(0, CoverConfigurators.NONE),
    RAINFED_CROPLAND(10, CoverConfigurators.NONE), // TODO
    HERBACEOUS_COVER(11, CoverConfigurators.NONE), // TODO
    TREE_OR_SHRUB_COVER(12, CoverConfigurators.NONE), // TODO
    IRRIGATED_CROPLAND(20, CoverConfigurators.NONE), // TODO
    CROPLAND_WITH_VEGETATION(30, CoverConfigurators.NONE), // TODO
    VEGETATION_WITH_CROPLAND(40, CoverConfigurators.NONE), // TODO
    BROADLEAF_EVERGREEN(50, ForestConfigurators.BROADLEAF_EVERGREEN),
    BROADLEAF_DECIDUOUS(60, ForestConfigurators.BROADLEAF_DECIDUOUS),
    BROADLEAF_DECIDUOUS_CLOSED(61, ForestConfigurators.BROADLEAF_DECIDUOUS_CLOSED),
    BROADLEAF_DECIDUOUS_OPEN(62, ForestConfigurators.BROADLEAF_DECIDUOUS_OPEN),
    NEEDLEAF_EVERGREEN(70, ForestConfigurators.NEEDLELEAF_EVERGREEN),
    NEEDLEAF_EVERGREEN_CLOSED(71, ForestConfigurators.NEEDLELEAF_EVERGREEN_CLOSED),
    NEEDLEAF_EVERGREEN_OPEN(72, ForestConfigurators.NEEDLELEAF_EVERGREEN_OPEN),
    NEEDLEAF_DECIDUOUS(80, ForestConfigurators.NEEDLELEAF_DECIDUOUS),
    NEEDLEAF_DECIDUOUS_CLOSED(81, ForestConfigurators.NEEDLELEAF_DECIDUOUS_CLOSED),
    NEEDLEAF_DECIDUOUS_OPEN(82, ForestConfigurators.NEEDLELEAF_DECIDUOUS_OPEN),
    MIXED_LEAF_TYPE(90, ForestConfigurators.MIXED_LEAF_TYPE),
    TREE_AND_SHRUB_WITH_HERBACEOUS_COVER(100, CoverConfigurators.NONE), // TODO
    HERBACEOUS_COVER_WITH_TREE_AND_SHRUB(110, CoverConfigurators.NONE), // TODO
    SHRUBLAND(120, CoverConfigurators.NONE), // TODO
    SHRUBLAND_EVERGREEN(121, CoverConfigurators.NONE), // TODO
    SHRUBLAND_DECIDUOUS(122, CoverConfigurators.NONE), // TODO
    GRASSLAND(130, CoverConfigurators.NONE), // TODO
    LICHENS_AND_MOSSES(140, CoverConfigurators.NONE), // TODO
    SPARSE_VEGETATION(150, CoverConfigurators.NONE), // TODO
    SPARSE_TREE(151, CoverConfigurators.NONE), // TODO
    SPARSE_SHRUB(152, CoverConfigurators.NONE), // TODO
    SPARSE_HERBACEOUS_COVER(153, CoverConfigurators.NONE), // TODO
    FRESH_FLOODED_FOREST(160, CoverConfigurators.FLOODED), // TODO
    SALINE_FLOODED_FOREST(170, CoverConfigurators.FLOODED), // TODO
    FLOODED_VEGETATION(180, CoverConfigurators.FLOODED), // TODO
    URBAN(190, CoverConfigurators.NONE),
    BARE(200, CoverConfigurators.NONE), // TODO
    BARE_CONSOLIDATED(201, CoverConfigurators.NONE), // TODO
    BARE_UNCONSOLIDATED(202, CoverConfigurators.NONE), // TODO
    WATER(210, CoverConfigurators.WATER),
    PERMANENT_SNOW(220, CoverConfigurators.SNOWY);

    public static final CoverClassification[] TYPES = CoverClassification.values();
    public static final CoverClassification[] CLASSIFICATION_IDS = new CoverClassification[256];

    private final byte id;
    private final CoverConfig config;

    CoverClassification(int id, CoverConfigurator configurator) {
        this.id = (byte) (id & 0xFF);

        this.config = new CoverConfig();
        configurator.configure(this.config);
    }

    public int getId() {
        return this.id;
    }

    public CoverConfig getConfig() {
        return this.config;
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
