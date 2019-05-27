package net.gegy1000.earth.server.world.cover;

public final class CoverIds {
    public static final int NO_DATA = 0;
    public static final int RAINFED_CROPLAND = 10;
    public static final int HERBACEOUS_COVER = 11;
    public static final int TREE_OR_SHRUB_COVER = 12;
    public static final int IRRIGATED_CROPLAND = 20;
    public static final int CROPLAND_WITH_VEGETATION = 30;
    public static final int VEGETATION_WITH_CROPLAND = 40;
    public static final int BROADLEAF_EVERGREEN = 50;
    public static final int BROADLEAF_DECIDUOUS = 60;
    public static final int BROADLEAF_DECIDUOUS_CLOSED = 61;
    public static final int BROADLEAF_DECIDUOUS_OPEN = 62;
    public static final int NEEDLEAF_EVERGREEN = 70;
    public static final int NEEDLEAF_EVERGREEN_CLOSED = 71;
    public static final int NEEDLEAF_EVERGREEN_OPEN = 72;
    public static final int NEEDLEAF_DECIDUOUS = 80;
    public static final int NEEDLEAF_DECIDUOUS_CLOSED = 81;
    public static final int NEEDLEAF_DECIDUOUS_OPEN = 82;
    public static final int MIXED_LEAF_TYPE = 90;
    public static final int TREE_AND_SHRUB_WITH_HERBACEOUS_COVER = 100;
    public static final int HERBACEOUS_COVER_WITH_TREE_AND_SHRUB = 110;
    public static final int SHRUBLAND = 120;
    public static final int SHRUBLAND_EVERGREEN = 121;
    public static final int SHRUBLAND_DECIDUOUS = 122;
    public static final int GRASSLAND = 130;
    public static final int LICHENS_AND_MOSSES = 140;
    public static final int SPARSE_VEGETATION = 150;
    public static final int SPARSE_TREE = 151;
    public static final int SPARSE_SHRUB = 152;
    public static final int SPARSE_HERBACEOUS_COVER = 153;
    public static final int FRESH_FLOODED_FOREST = 160;
    public static final int SALINE_FLOODED_FOREST = 170;
    public static final int FLOODED_VEGETATION = 180;
    public static final int URBAN = 190;
    public static final int BARE = 200;
    public static final int BARE_CONSOLIDATED = 201;
    public static final int BARE_UNCONSOLIDATED = 202;
    public static final int WATER = 210;
    public static final int PERMANENT_SNOW = 220;

    private CoverIds() {
    }

    public static Cover get(int id) {
        switch (id) {
            case NO_DATA:
                return Cover.NONE;
            case RAINFED_CROPLAND:
                return Cover.RAINFED_CROPLAND;
            case HERBACEOUS_COVER:
                return Cover.HERBACEOUS_COVER;
            case TREE_OR_SHRUB_COVER:
                return Cover.TREE_OR_SHRUB_COVER;
            case IRRIGATED_CROPLAND:
                return Cover.IRRIGATED_CROPLAND;
            case CROPLAND_WITH_VEGETATION:
                return Cover.CROPLAND_WITH_VEGETATION;
            case VEGETATION_WITH_CROPLAND:
                return Cover.VEGETATION_WITH_CROPLAND;
            case BROADLEAF_EVERGREEN:
                return Cover.BROADLEAF_EVERGREEN;
            case BROADLEAF_DECIDUOUS:
                return Cover.BROADLEAF_DECIDUOUS;
            case BROADLEAF_DECIDUOUS_CLOSED:
                return Cover.BROADLEAF_DECIDUOUS_CLOSED;
            case BROADLEAF_DECIDUOUS_OPEN:
                return Cover.BROADLEAF_DECIDUOUS_OPEN;
            case NEEDLEAF_EVERGREEN:
                return Cover.NEEDLEAF_EVERGREEN;
            case NEEDLEAF_EVERGREEN_CLOSED:
                return Cover.NEEDLEAF_EVERGREEN_CLOSED;
            case NEEDLEAF_EVERGREEN_OPEN:
                return Cover.NEEDLEAF_EVERGREEN_OPEN;
            case NEEDLEAF_DECIDUOUS:
                return Cover.NEEDLEAF_DECIDUOUS;
            case NEEDLEAF_DECIDUOUS_CLOSED:
                return Cover.NEEDLEAF_DECIDUOUS_CLOSED;
            case NEEDLEAF_DECIDUOUS_OPEN:
                return Cover.NEEDLEAF_DECIDUOUS_OPEN;
            case MIXED_LEAF_TYPE:
                return Cover.MIXED_LEAF_TYPE;
            case TREE_AND_SHRUB_WITH_HERBACEOUS_COVER:
                return Cover.TREE_AND_SHRUB_WITH_HERBACEOUS_COVER;
            case HERBACEOUS_COVER_WITH_TREE_AND_SHRUB:
                return Cover.HERBACEOUS_COVER_WITH_TREE_AND_SHRUB;
            case SHRUBLAND:
                return Cover.SHRUBLAND;
            case SHRUBLAND_EVERGREEN:
                return Cover.SHRUBLAND_EVERGREEN;
            case SHRUBLAND_DECIDUOUS:
                return Cover.SHRUBLAND_DECIDUOUS;
            case GRASSLAND:
                return Cover.GRASSLAND;
            case LICHENS_AND_MOSSES:
                return Cover.LICHENS_AND_MOSSES;
            case SPARSE_VEGETATION:
                return Cover.SPARSE_VEGETATION;
            case SPARSE_TREE:
                return Cover.SPARSE_TREE;
            case SPARSE_SHRUB:
                return Cover.SPARSE_SHRUB;
            case SPARSE_HERBACEOUS_COVER:
                return Cover.SPARSE_HERBACEOUS_COVER;
            case FRESH_FLOODED_FOREST:
                return Cover.FRESH_FLOODED_FOREST;
            case SALINE_FLOODED_FOREST:
                return Cover.SALINE_FLOODED_FOREST;
            case FLOODED_VEGETATION:
                return Cover.FLOODED_VEGETATION;
            case URBAN:
                return Cover.URBAN;
            case BARE:
                return Cover.BARE;
            case BARE_CONSOLIDATED:
                return Cover.BARE_CONSOLIDATED;
            case BARE_UNCONSOLIDATED:
                return Cover.BARE_UNCONSOLIDATED;
            case WATER:
                return Cover.WATER;
            case PERMANENT_SNOW:
                return Cover.PERMANENT_SNOW;
        }
        return Cover.NONE;
    }
}
