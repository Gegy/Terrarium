package net.gegy1000.earth.server.world.pipeline.data;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;

import java.util.concurrent.CompletableFuture;

public final class WaterProducer {
    public static final int TYPE_MASK = 0x3;
    public static final short LAND = 0;
    public static final short OCEAN = 1;
    public static final short RIVER = 2;
    public static final short BANK = 3;

    protected static final short BANK_UP_FLAG = 0b100;
    protected static final short BANK_DOWN_FLAG = 0b1000;
    protected static final short FREE_FLOOD_FLAG = 0b10000;
    protected static final short CENTER_FLAG = 0b100000;

    public static DataOp<ShortRaster> produceBanks(DataOp<ShortRaster> height, DataOp<ObjRaster<Cover>> cover) {
        return DataOp.of((engine, view) -> {
            CompletableFuture<ObjRaster<Cover>> coverFuture = engine.load(cover, view);
            CompletableFuture<ShortRaster> heightFuture = engine.load(height, view);

            return CompletableFuture.allOf(coverFuture, heightFuture)
                    .thenApply(v -> {
                        ObjRaster<Cover> coverRaster = coverFuture.join();
                        ShortRaster heightRaster = heightFuture.join();

                        ShortRaster result = ShortRaster.create(view);

                        for (int localY = 0; localY < view.getHeight(); localY++) {
                            for (int localX = 0; localX < view.getWidth(); localX++) {
                                Cover sampledCover = coverRaster.get(localX, localY);
                                if (sampledCover == Cover.WATER) {
                                    short sampledHeight = heightRaster.get(localX, localY);
                                    int bankType = sampledHeight <= 1 ? OCEAN : RIVER;
                                    result.set(localX, localY, (short) bankType);
                                }
                            }
                        }

                        return result;
                    });
        });
    }

    public static DataOp<WaterRaster> produceWater(DataOp<ShortRaster> bank) {
        return DataOp.of((engine, view) -> {
            CompletableFuture<ShortRaster> bankFuture = engine.load(bank, view);
            return bankFuture.thenApply(bankRaster -> {
                WaterRaster result = WaterRaster.create(view);

                for (int localZ = 0; localZ < view.getHeight(); localZ++) {
                    for (int localX = 0; localX < view.getWidth(); localX++) {
                        short waterValue = bankRaster.get(localX, localZ);
                        int waterType = waterValue & TYPE_MASK;
                        switch (waterType) {
                            case LAND:
                            case BANK:
                                result.setWaterType(localX, localZ, WaterRaster.LAND);
                                break;
                            case RIVER:
                                if ((waterValue & CENTER_FLAG) != 0) {
                                    result.setWaterType(localX, localZ, WaterRaster.RIVER_CENTER);
                                } else {
                                    result.setWaterType(localX, localZ, WaterRaster.RIVER);
                                }
                                break;
                            case OCEAN:
                                result.setWaterType(localX, localZ, WaterRaster.OCEAN);
                                break;
                        }
                    }
                }

                return result;
            });
        });
    }
}
