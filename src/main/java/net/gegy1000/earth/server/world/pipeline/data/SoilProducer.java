package net.gegy1000.earth.server.world.pipeline.data;

import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.earth.server.world.pipeline.source.tile.SoilClassificationRaster;
import net.gegy1000.earth.server.world.pipeline.source.tile.SoilRaster;
import net.gegy1000.earth.server.world.soil.SoilClassification;
import net.gegy1000.earth.server.world.soil.SoilConfig;
import net.gegy1000.earth.server.world.soil.SoilConfigs;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.data.DataFuture;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.CoverRaster;

import java.util.concurrent.CompletableFuture;

public final class SoilProducer {
    public static DataFuture<SoilRaster> produce(DataFuture<CoverRaster> coverClassification, DataFuture<SoilClassificationRaster> soilClassification) {
        return DataFuture.of((engine, view) -> {
            CompletableFuture<CoverRaster> coverFuture = engine.load(coverClassification, view);
            CompletableFuture<SoilClassificationRaster> soilFuture = engine.load(soilClassification, view);
            return CompletableFuture.allOf(coverFuture, soilFuture)
                    .thenApply(v -> {
                        CoverRaster cover = coverFuture.join();
                        SoilClassificationRaster soil = soilFuture.join();

                        SoilRaster result = new SoilRaster(view);
                        for (int y = 0; y < view.getHeight(); y++) {
                            for (int x = 0; x < view.getWidth(); x++) {
                                CoverType<?> sampledCover = cover.get(x, y);
                                SoilClassification sampledSoil = soil.get(x, y);
                                result.set(x, y, produceSoilConfig(sampledCover, sampledSoil));
                            }
                        }

                        return result;
                    });
        });
    }

    private static SoilConfig produceSoilConfig(CoverType<?> cover, SoilClassification soil) {
        if (cover == EarthCoverTypes.SNOW) {
            return SoilConfigs.PERMANENT_SNOW;
        }

        switch (soil) {
            case CHERNOZEMS:
            case CAMBISOLS:
            case HISTOSOLS:
            case PHAEOZEMS:
            case UMBRISOLS:
                return SoilConfigs.LOAMY_SOIL;
            case CRYOSOLS:
            case FERRALSOLS:
            case KASTANOZEMS:
                return SoilConfigs.NORMAL_SOIL;
            case GYPSISOLS:
            case SOLONCHAKS:
            case STAGNOSOLS:
                return SoilConfigs.GRAVELLY;
            case ACRISOLS:
            case ALISOLS:
                return SoilConfigs.ACRISOL;
            case PLANOSOLS:
            case SOLONETZ:
                return SoilConfigs.COARSE_AND_CLAY;
            case ARENOSOLS:
            case CALCISOLS:
                return SoilConfigs.SANDY;
            case ANDOSOLS:
            case VERTISOLS:
                return SoilConfigs.BLACK_SOIL;
            case ALBELUVISOLS:
                return SoilConfigs.ALBELUVISOL;
            case DURISOLS:
                return SoilConfigs.SANDY_DIRT;
            case FLUVISOLS:
                return SoilConfigs.SILTY_SOIL;
            case GLEYSOLS:
                return SoilConfigs.CLAY;
            case LEPTOSOLS:
                return SoilConfigs.LEPTOSOL;
            case LIXISOLS:
                return SoilConfigs.LIXISOL;
            case LUVISOLS:
                return SoilConfigs.LUVISOL;
            case NITISOLS:
                return SoilConfigs.NITISOL;
            case PLINTHOSOLS:
                return SoilConfigs.PLINTHOSOL;
            case PODZOLS:
                return SoilConfigs.PODZOL;
            case REGOSOLS:
                return SoilConfigs.REGOSOL;
        }

        return SoilConfigs.NORMAL_SOIL;
    }
}
