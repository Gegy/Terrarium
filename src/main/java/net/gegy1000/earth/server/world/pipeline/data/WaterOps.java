package net.gegy1000.earth.server.world.pipeline.data;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;

import java.util.concurrent.CompletableFuture;

public final class WaterOps {
    public static DataOp<WaterRaster> levelWater(DataOp<WaterRaster> water, int seaLevel) {
        return DataOp.of((engine, view) -> {
            CompletableFuture<WaterRaster> waterFuture = engine.load(water, view);

            return waterFuture.thenApply(waterRaster -> {
                for (int localZ = 0; localZ < waterRaster.getHeight(); localZ++) {
                    for (int localX = 0; localX < waterRaster.getWidth(); localX++) {
                        int waterType = waterRaster.getWaterType(localX, localZ);
                        if (waterType == WaterRaster.SEA) {
                            waterRaster.setWaterLevel(localX, localZ, seaLevel);
                        }
                    }
                }

                return waterRaster;
            });
        });
    }

    // TODO: Carve smoothed edges
    public static DataOp<ShortRaster> applyToHeight(DataOp<ShortRaster> height, DataOp<WaterRaster> water, int seaDepth) {
        return DataOp.of((engine, view) -> {
            CompletableFuture<ShortRaster> heightFuture = engine.load(height, view);
            CompletableFuture<WaterRaster> waterFuture = engine.load(water, view);

            return CompletableFuture.allOf(heightFuture, waterFuture)
                    .thenApply(v -> {
                        ShortRaster heightRaster = heightFuture.join();
                        WaterRaster waterRaster = waterFuture.join();

                        heightRaster.transform((source, x, y) -> {
                            int waterType = waterRaster.getWaterType(x, y);
                            if (waterType == WaterRaster.SEA) {
                                int waterLevel = waterRaster.getWaterLevel(x, y);
                                double carvedHeight = waterLevel - seaDepth;
                                return (short) Math.round(carvedHeight);
                            }

                            return source;
                        });

                        return heightRaster;
                    });
        });
    }

    // TODO: Properly select cover type for filled in space!
    public static DataOp<ObjRaster<Cover>> applyToCover(DataOp<ObjRaster<Cover>> cover, DataOp<WaterRaster> water) {
        return DataOp.of((engine, view) -> {
            CompletableFuture<ObjRaster<Cover>> coverFuture = engine.load(cover, view);
            CompletableFuture<WaterRaster> waterFuture = engine.load(water, view);

            return CompletableFuture.allOf(coverFuture, waterFuture)
                    .thenApply(v -> {
                        ObjRaster<Cover> coverRaster = coverFuture.join();
                        WaterRaster waterRaster = waterFuture.join();

                        coverRaster.transform((source, x, y) -> {
                            int sampleType = waterRaster.getWaterType(x, y);
                            if (WaterRaster.isWater(sampleType)) {
                                return Cover.WATER;
                            } else if (source == Cover.WATER) {
                                return Cover.NO_DATA;
                            }
                            return source;
                        });

                        return coverRaster;
                    });
        });
    }
}
