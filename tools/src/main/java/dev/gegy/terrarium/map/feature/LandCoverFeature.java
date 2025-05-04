package dev.gegy.terrarium.map.feature;

import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.earth.EarthLayers;
import dev.gegy.terrarium.backend.earth.cover.Cover;
import dev.gegy.terrarium.backend.raster.EnumRaster;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record LandCoverFeature() implements RasterMapFeature<EnumRaster<Cover>> {
    @Override
    public CompletableFuture<Optional<EnumRaster<Cover>>> sample(final EarthLayers layers, final GeoView view) {
        return layers.landCover().getExact(view);
    }

    @Override
    public int getColor(final EnumRaster<Cover> raster, final int x, final int y) {
        return switch (raster.get(x, y)) {
            case RAINFED_CROPLAND, HERBACEOUS_COVER -> 0xffff64;
            case TREE_OR_SHRUB_COVER -> 0xffff00;
            case IRRIGATED_CROPLAND -> 0xaaf0f0;
            case CROPLAND_WITH_VEGETATION -> 0xdcf064;
            case VEGETATION_WITH_CROPLAND -> 0xc8c864;
            case BROADLEAF_EVERGREEN -> 0x006400;
            case BROADLEAF_DECIDUOUS -> 0x00a000;
            case BROADLEAF_DECIDUOUS_CLOSED -> 0x00a000;
            case BROADLEAF_DECIDUOUS_OPEN -> 0xaac800;
            case NEEDLE_LEAF_EVERGREEN -> 0x003c00;
            case NEEDLE_LEAF_EVERGREEN_CLOSED -> 0x003c00;
            case NEEDLE_LEAF_EVERGREEN_OPEN -> 0x005000;
            case NEEDLE_LEAF_DECIDUOUS -> 0x285000;
            case NEEDLE_LEAF_DECIDUOUS_CLOSED -> 0x285000;
            case NEEDLE_LEAF_DECIDUOUS_OPEN -> 0x286400;
            case MIXED_LEAF_TYPE -> 0x788200;
            case TREE_AND_SHRUB_WITH_HERBACEOUS_COVER -> 0x8ca000;
            case HERBACEOUS_COVER_WITH_TREE_AND_SHRUB -> 0xbe9600;
            case SHRUBLAND -> 0x966400;
            case SHRUBLAND_EVERGREEN -> 0x784b00;
            case SHRUBLAND_DECIDUOUS -> 0x966400;
            case GRASSLAND -> 0xffb432;
            case LICHENS_AND_MOSSES -> 0xffdcd2;
            case SPARSE_VEGETATION -> 0xffebaf;
            case SPARSE_TREE -> 0xffebaf;
            case SPARSE_SHRUB -> 0xffd278;
            case SPARSE_HERBACEOUS_COVER -> 0xffebaf;
            case FRESH_FLOODED_FOREST -> 0x00785a;
            case SALINE_FLOODED_FOREST -> 0x009678;
            case FLOODED_VEGETATION -> 0x00dc82;
            case URBAN -> 0xc31400;
            case BARE -> 0xfff5d7;
            case BARE_CONSOLIDATED -> 0xdcdcdc;
            case BARE_UNCONSOLIDATED -> 0xfff5d7;
            case WATER -> 0x0046c8;
            case PERMANENT_SNOW -> 0xffffff;
            default -> 0;
        };
    }
}
