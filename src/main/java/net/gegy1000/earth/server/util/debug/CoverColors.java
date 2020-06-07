package net.gegy1000.earth.server.util.debug;

import net.gegy1000.earth.server.world.cover.Cover;

public final class CoverColors {
    public static int get(Cover cover) {
        switch (cover) {
            case RAINFED_CROPLAND:
            case HERBACEOUS_COVER: return 0xFFFF64;
            case TREE_OR_SHRUB_COVER: return 0xFFFF00;
            case IRRIGATED_CROPLAND: return 0xAAF0F0;
            case CROPLAND_WITH_VEGETATION: return 0xDCF064;
            case VEGETATION_WITH_CROPLAND: return 0xC8C864;
            case BROADLEAF_EVERGREEN: return 0x006400;
            case BROADLEAF_DECIDUOUS: return 0x00A000;
            case BROADLEAF_DECIDUOUS_CLOSED: return 0x00A000;
            case BROADLEAF_DECIDUOUS_OPEN: return 0xAAC800;
            case NEEDLEAF_EVERGREEN: return 0x003C00;
            case NEEDLEAF_EVERGREEN_CLOSED: return 0x003C00;
            case NEEDLEAF_EVERGREEN_OPEN: return 0x005000;
            case NEEDLEAF_DECIDUOUS: return 0x285000;
            case NEEDLEAF_DECIDUOUS_CLOSED: return 0x285000;
            case NEEDLEAF_DECIDUOUS_OPEN: return 0x286400;
            case MIXED_LEAF_TYPE: return 0x788200;
            case TREE_AND_SHRUB_WITH_HERBACEOUS_COVER: return 0x8CA000;
            case HERBACEOUS_COVER_WITH_TREE_AND_SHRUB: return 0xBE9600;
            case SHRUBLAND: return 0x966400;
            case SHRUBLAND_EVERGREEN: return 0x784B00;
            case SHRUBLAND_DECIDUOUS: return 0x966400;
            case GRASSLAND: return 0xFFB432;
            case LICHENS_AND_MOSSES: return 0xFFDCD2;
            case SPARSE_VEGETATION: return 0xFFEBAF;
            case SPARSE_TREE: return 0xFFEBAF;
            case SPARSE_SHRUB: return 0xFFD278;
            case SPARSE_HERBACEOUS_COVER: return 0xFFEBAF;
            case FRESH_FLOODED_FOREST: return 0x00785A;
            case SALINE_FLOODED_FOREST: return 0x009678;
            case FLOODED_VEGETATION: return 0x00DC82;
            case URBAN: return 0xC31400;
            case BARE: return 0xFFF5D7;
            case BARE_CONSOLIDATED: return 0xDCDCDC;
            case BARE_UNCONSOLIDATED: return 0xFFF5D7;
            case WATER: return 0x0046C8;
            case PERMANENT_SNOW: return 0xFFFFFF;
            default:
            case NO: return 0;
        }
    }
}
