package net.gegy1000.earth.server.world.cover;

import net.gegy1000.earth.server.event.AddCoverMarkersEvent;
import net.minecraftforge.common.MinecraftForge;

public final class CoverMarkers {
    public static final CoverMarker WATER = create();
    public static final CoverMarker FROZEN = create();
    public static final CoverMarker FLOODED = create();
    public static final CoverMarker BARREN = create();
    public static final CoverMarker NO_VEGETATION = create();

    public static final CoverMarker MODERATE_TREES = create();

    public static final CoverMarker FOREST = create();

    public static final CoverMarker OPEN_FOREST = create();
    public static final CoverMarker CLOSED_FOREST = create();
    public static final CoverMarker CLOSED_TO_OPEN_FOREST = create();

    public static final CoverMarker PLAINS = create();
    public static final CoverMarker DENSE_SHRUBS = create();
    public static final CoverMarker SPARSE_SHRUBS = create();

    public static final CoverMarker DECIDUOUS = create();
    public static final CoverMarker EVERGREEN = create();
    public static final CoverMarker BROADLEAF = create();
    public static final CoverMarker NEEDLELEAF = create();

    public static final CoverMarker DENSE_GRASS = create();
    public static final CoverMarker HARSH = create();
    public static final CoverMarker CONSOLIDATED = create();

    public static CoverMarker create() {
        return new CoverMarker();
    }

    public static void register() {
        WATER.add(Cover.WATER);
        FROZEN.add(Cover.PERMANENT_SNOW);
        FLOODED.add(Cover.FLOODED_VEGETATION, Cover.FRESH_FLOODED_FOREST, Cover.SALINE_FLOODED_FOREST);
        BARREN.add(Cover.BARE, Cover.BARE_CONSOLIDATED, Cover.BARE_UNCONSOLIDATED);

        NO_VEGETATION.add(Cover.WATER, Cover.PERMANENT_SNOW);

        FOREST.add(
                Cover.MIXED_LEAF_TYPE,
                Cover.FRESH_FLOODED_FOREST, Cover.SALINE_FLOODED_FOREST,
                Cover.BROADLEAF_DECIDUOUS, Cover.BROADLEAF_DECIDUOUS_CLOSED, Cover.BROADLEAF_DECIDUOUS_OPEN,
                Cover.BROADLEAF_EVERGREEN,
                Cover.NEEDLEAF_DECIDUOUS, Cover.NEEDLEAF_DECIDUOUS_CLOSED, Cover.NEEDLEAF_DECIDUOUS_OPEN,
                Cover.NEEDLEAF_EVERGREEN, Cover.NEEDLEAF_EVERGREEN_CLOSED, Cover.NEEDLEAF_EVERGREEN_OPEN
        );

        CLOSED_TO_OPEN_FOREST.add(
                Cover.BROADLEAF_DECIDUOUS, Cover.BROADLEAF_EVERGREEN,
                Cover.NEEDLEAF_DECIDUOUS, Cover.NEEDLEAF_EVERGREEN
        );

        OPEN_FOREST.add(Cover.BROADLEAF_DECIDUOUS_OPEN, Cover.NEEDLEAF_EVERGREEN_OPEN, Cover.NEEDLEAF_DECIDUOUS_OPEN);
        CLOSED_FOREST.add(Cover.BROADLEAF_DECIDUOUS_CLOSED, Cover.NEEDLEAF_EVERGREEN_CLOSED, Cover.NEEDLEAF_DECIDUOUS_CLOSED);

        CLOSED_TO_OPEN_FOREST.add(Cover.SALINE_FLOODED_FOREST);
        CLOSED_FOREST.add(Cover.FRESH_FLOODED_FOREST);

        DECIDUOUS.add(
                Cover.BROADLEAF_DECIDUOUS, Cover.BROADLEAF_DECIDUOUS_CLOSED, Cover.BROADLEAF_DECIDUOUS_OPEN,
                Cover.NEEDLEAF_DECIDUOUS, Cover.NEEDLEAF_DECIDUOUS_CLOSED, Cover.NEEDLEAF_DECIDUOUS_OPEN,
                Cover.SHRUBLAND_DECIDUOUS
        );

        EVERGREEN.add(
                Cover.BROADLEAF_EVERGREEN,
                Cover.NEEDLEAF_EVERGREEN, Cover.NEEDLEAF_EVERGREEN_CLOSED, Cover.NEEDLEAF_EVERGREEN_OPEN,
                Cover.SHRUBLAND_EVERGREEN
        );

        BROADLEAF.add(
                Cover.BROADLEAF_EVERGREEN,
                Cover.BROADLEAF_DECIDUOUS, Cover.BROADLEAF_DECIDUOUS_CLOSED, Cover.BROADLEAF_DECIDUOUS_OPEN
        );

        NEEDLELEAF.add(
                Cover.NEEDLEAF_EVERGREEN, Cover.NEEDLEAF_EVERGREEN_CLOSED, Cover.NEEDLEAF_EVERGREEN_OPEN,
                Cover.NEEDLEAF_DECIDUOUS, Cover.NEEDLEAF_DECIDUOUS_CLOSED, Cover.NEEDLEAF_DECIDUOUS_OPEN
        );

        PLAINS.add(
                Cover.VEGETATION_WITH_CROPLAND, Cover.CROPLAND_WITH_VEGETATION,
                Cover.FLOODED_VEGETATION, Cover.HERBACEOUS_COVER,
                Cover.GRASSLAND, Cover.LICHENS_AND_MOSSES,
                Cover.RAINFED_CROPLAND, Cover.IRRIGATED_CROPLAND,
                Cover.HERBACEOUS_COVER_WITH_TREE_AND_SHRUB, Cover.SPARSE_SHRUB,
                Cover.SHRUBLAND, Cover.SHRUBLAND_EVERGREEN, Cover.SHRUBLAND_DECIDUOUS
        );

        DENSE_SHRUBS.add(
                Cover.TREE_OR_SHRUB_COVER,
                Cover.TREE_AND_SHRUB_WITH_HERBACEOUS_COVER,
                Cover.HERBACEOUS_COVER_WITH_TREE_AND_SHRUB,
                Cover.SHRUBLAND, Cover.SHRUBLAND_EVERGREEN, Cover.SHRUBLAND_DECIDUOUS
        );
        DENSE_SHRUBS.addAll(CLOSED_FOREST);

        SPARSE_SHRUBS.add(
                Cover.SPARSE_SHRUB,
                Cover.VEGETATION_WITH_CROPLAND, Cover.CROPLAND_WITH_VEGETATION,
                Cover.FLOODED_VEGETATION, Cover.HERBACEOUS_COVER
        );
        SPARSE_SHRUBS.addAll(OPEN_FOREST);

        MODERATE_TREES.add(
                Cover.TREE_OR_SHRUB_COVER,
                Cover.TREE_AND_SHRUB_WITH_HERBACEOUS_COVER,
                Cover.HERBACEOUS_COVER_WITH_TREE_AND_SHRUB,
                Cover.FLOODED_VEGETATION,
                Cover.VEGETATION_WITH_CROPLAND,
                Cover.CROPLAND_WITH_VEGETATION
        );

        DENSE_GRASS.add(
                Cover.GRASSLAND,
                Cover.LICHENS_AND_MOSSES,
                Cover.HERBACEOUS_COVER_WITH_TREE_AND_SHRUB,
                Cover.HERBACEOUS_COVER,
                Cover.SHRUBLAND, Cover.SHRUBLAND_EVERGREEN, Cover.SHRUBLAND_DECIDUOUS,
                Cover.VEGETATION_WITH_CROPLAND, Cover.CROPLAND_WITH_VEGETATION,
                Cover.RAINFED_CROPLAND, Cover.IRRIGATED_CROPLAND
        );
        DENSE_GRASS.addAll(FLOODED);

        HARSH.add(
                Cover.BARE, Cover.BARE_CONSOLIDATED, Cover.BARE_UNCONSOLIDATED,
                Cover.SALINE_FLOODED_FOREST, Cover.SPARSE_VEGETATION
        );

        CONSOLIDATED.add(Cover.BARE_CONSOLIDATED);

        MinecraftForge.EVENT_BUS.post(new AddCoverMarkersEvent());
    }
}
