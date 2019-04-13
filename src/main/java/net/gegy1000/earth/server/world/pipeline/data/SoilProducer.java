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
            case ANDOSOLS:
            case VERTISOLS:
                return SoilConfigs.LOAMY_SOIL;
            case ACRISOLS:
            case ALISOLS:
            case PLANOSOLS:
            case SOLONETZ:
            case GLEYSOLS:
            case FLUVISOLS:
            case LIXISOLS:
            case LUVISOLS:
            case NITISOLS:
                return SoilConfigs.SILTY_SOIL;
            case CRYOSOLS:
            case FERRALSOLS:
            case KASTANOZEMS:
                return SoilConfigs.NORMAL_SOIL;
            case GYPSISOLS:
            case SOLONCHAKS:
            case STAGNOSOLS:
                return SoilConfigs.GRAVEL;
            case ARENOSOLS:
            case CALCISOLS:
            case DURISOLS:
                return SoilConfigs.SANDY_SOIL;
            case ALBELUVISOLS:
                return SoilConfigs.ALBELUVISOL;
            case LEPTOSOLS:
                return SoilConfigs.LEPTOSOL;
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
