package net.gegy1000.earth.server.world.cover;

public enum Cover {
    NONE(CoverConfigurators.NONE),
    RAINFED_CROPLAND(CoverConfigurators.NONE), // TODO
    HERBACEOUS_COVER(CoverConfigurators.NONE), // TODO
    TREE_OR_SHRUB_COVER(CoverConfigurators.NONE), // TODO
    IRRIGATED_CROPLAND(CoverConfigurators.NONE), // TODO
    CROPLAND_WITH_VEGETATION(CoverConfigurators.NONE), // TODO
    VEGETATION_WITH_CROPLAND(CoverConfigurators.NONE), // TODO
    BROADLEAF_EVERGREEN(ForestConfigurators.BROADLEAF_EVERGREEN),
    BROADLEAF_DECIDUOUS(ForestConfigurators.BROADLEAF_DECIDUOUS),
    BROADLEAF_DECIDUOUS_CLOSED(ForestConfigurators.BROADLEAF_DECIDUOUS_CLOSED),
    BROADLEAF_DECIDUOUS_OPEN(ForestConfigurators.BROADLEAF_DECIDUOUS_OPEN),
    NEEDLEAF_EVERGREEN(ForestConfigurators.NEEDLELEAF_EVERGREEN),
    NEEDLEAF_EVERGREEN_CLOSED(ForestConfigurators.NEEDLELEAF_EVERGREEN_CLOSED),
    NEEDLEAF_EVERGREEN_OPEN(ForestConfigurators.NEEDLELEAF_EVERGREEN_OPEN),
    NEEDLEAF_DECIDUOUS(ForestConfigurators.NEEDLELEAF_DECIDUOUS),
    NEEDLEAF_DECIDUOUS_CLOSED(ForestConfigurators.NEEDLELEAF_DECIDUOUS_CLOSED),
    NEEDLEAF_DECIDUOUS_OPEN(ForestConfigurators.NEEDLELEAF_DECIDUOUS_OPEN),
    MIXED_LEAF_TYPE(ForestConfigurators.MIXED_LEAF_TYPE),
    TREE_AND_SHRUB_WITH_HERBACEOUS_COVER(CoverConfigurators.NONE), // TODO
    HERBACEOUS_COVER_WITH_TREE_AND_SHRUB(CoverConfigurators.NONE), // TODO
    SHRUBLAND(CoverConfigurators.NONE), // TODO
    SHRUBLAND_EVERGREEN(CoverConfigurators.NONE), // TODO
    SHRUBLAND_DECIDUOUS(CoverConfigurators.NONE), // TODO
    GRASSLAND(CoverConfigurators.NONE), // TODO
    LICHENS_AND_MOSSES(CoverConfigurators.NONE), // TODO
    SPARSE_VEGETATION(CoverConfigurators.NONE), // TODO
    SPARSE_TREE(CoverConfigurators.NONE), // TODO
    SPARSE_SHRUB(CoverConfigurators.NONE), // TODO
    SPARSE_HERBACEOUS_COVER(CoverConfigurators.NONE), // TODO
    FRESH_FLOODED_FOREST(CoverConfigurators.FLOODED), // TODO
    SALINE_FLOODED_FOREST(CoverConfigurators.FLOODED), // TODO
    FLOODED_VEGETATION(CoverConfigurators.FLOODED), // TODO
    URBAN(CoverConfigurators.NONE),
    BARE(CoverConfigurators.NONE), // TODO
    BARE_CONSOLIDATED(CoverConfigurators.NONE), // TODO
    BARE_UNCONSOLIDATED(CoverConfigurators.NONE), // TODO
    WATER(CoverConfigurators.WATER),
    PERMANENT_SNOW(CoverConfigurators.SNOWY);

    private final CoverConfig config;

    Cover(CoverConfigurator configurator) {
        this.config = new CoverConfig();
        configurator.configure(this.config);
    }

    public CoverConfig getConfig() {
        return this.config;
    }
}
