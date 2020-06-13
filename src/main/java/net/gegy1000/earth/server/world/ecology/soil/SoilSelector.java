package net.gegy1000.earth.server.world.ecology.soil;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.geography.Landform;

public final class SoilSelector {
    private static final int VERY_GRASSY_OCC = 40;
    private static final int GRASSY_OCC = 9;

    private SoilSelector() {
    }

    public static boolean isDesertLike(GrowthPredictors predictors) {
        SoilTexture texture = select(predictors);
        return texture == SoilTextures.DESERT_SAND || texture == SoilTextures.SAND
                || texture == SoilTextures.DESERT_RED_SAND || texture == SoilTextures.RED_SAND;
    }

    public static SoilTexture select(GrowthPredictors predictors) {
        // get the easy choices out of the way
        if (predictors.landform == Landform.SEA) return SoilTextures.OCEAN_FLOOR;
        if (predictors.landform == Landform.BEACH) return SoilTextures.BEACH;
        if (predictors.landform == Landform.LAKE_OR_RIVER) return SoilTextures.RIVER_BED;

        if (predictors.cover == Cover.PERMANENT_SNOW || predictors.soilSuborder == SoilSuborder.ICE) {
            return SoilTextures.SNOW;
        }

        if (predictors.soilSuborder == SoilSuborder.ROCK) return SoilTextures.ROCK;
        if (predictors.soilSuborder == SoilSuborder.SHIFTING_SAND) return SoilTextures.SAND;

        SoilTexture texture = selectLand(predictors);

        if (predictors.cover.is(CoverMarkers.CONSOLIDATED)) {
            texture = consolidate(texture);
        }

        if (predictors.cover.is(CoverMarkers.BARREN)) {
            if (texture == SoilTextures.SAND) return SoilTextures.DESERT_SAND;
            if (texture == SoilTextures.RED_SAND) return SoilTextures.DESERT_RED_SAND;
        }

        return texture;
    }

    private static SoilTexture selectLand(GrowthPredictors predictors) {
        // if organic carbon content is very high, we should definitely have grass here
        if (predictors.organicCarbonContent > VERY_GRASSY_OCC) {
            return SoilTextures.GRASS;
        }

        // TODO: in future use some form of vegetation index?
        boolean grassy = predictors.organicCarbonContent > GRASSY_OCC && !predictors.isVeryDry();

        if (grassy) {
            return selectGrassy(predictors);
        } else {
            return selectNotGrassy(predictors);
        }
    }

    private static SoilTexture selectGrassy(GrowthPredictors predictors) {
        boolean sandy = isSandy(predictors.soilSuborder);
        return sandy ? SoilTextures.GRASS_AND_SAND : SoilTextures.GRASS;
    }

    private static SoilTexture selectNotGrassy(GrowthPredictors predictors) {
        boolean sandy = isSandy(predictors.soilSuborder);

        // pretty much a grand-canyon special case: to be improved
        if (predictors.soilSuborder == SoilSuborder.ORTHENTS) {
            if (predictors.slope <= 50) {
                return SoilTextures.SAND;
            } else {
                return SoilTextures.MESA;
            }
        }

        return sandy ? SoilTextures.SAND : SoilTextures.GRASS_AND_DIRT;
    }

    private static boolean isSandy(SoilSuborder suborder) {
        switch (suborder) {
            case PSAMMENTS:
            case SALIDS:
            case ARENTS:
            case ARGIDS:
            case CAMBIDS:
            case USTEPTS:
            case USTOX:
            case XEREPTS:
            case XEROLLS:
            case XERALFS:
            case XERANDS:
                return true;
        }
        return false;
    }

    private static SoilTexture consolidate(SoilTexture texture) {
        if (texture == SoilTextures.SAND) {
            return SoilTextures.MESA;
        } else {
            return SoilTextures.ROCK;
        }
    }
}
