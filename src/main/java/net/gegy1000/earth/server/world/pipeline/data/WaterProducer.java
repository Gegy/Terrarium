package net.gegy1000.earth.server.world.pipeline.data;

import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.adapter.debug.DebugImageWriter;
import net.gegy1000.terrarium.server.world.pipeline.data.DataFuture;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.CoverRaster;
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

    public static final DebugImageWriter.ColorSelector<Short> BANK_DEBUG = value -> {
        if ((value & FREE_FLOOD_FLAG) != 0) {
            return 0x404040;
        } else if ((value & BANK_UP_FLAG) != 0) {
            return 0xFF0000;
        } else if ((value & BANK_DOWN_FLAG) != 0) {
            return 0xFFFF00;
        } else if ((value & CENTER_FLAG) != 0) {
            return 0x00FFFF;
        }
        int type = value & TYPE_MASK;
        switch (type) {
            case OCEAN:
                return 0x0000FF;
            case RIVER:
                return 0x00AAFF;
            case LAND:
                return 0x00FF00;
            case BANK:
                return 0xFFFFFF;
        }
        return 0;
    };

    public static DataFuture<ShortRaster> produceBanks(DataFuture<ShortRaster> height, DataFuture<CoverRaster> cover) {
        return DataFuture.of((engine, view) -> {
            CompletableFuture<CoverRaster> coverFuture = engine.load(cover, view);
            CompletableFuture<ShortRaster> heightFuture = engine.load(height, view);

            return CompletableFuture.allOf(coverFuture, heightFuture)
                    .thenApply(v -> {
                        CoverRaster coverRaster = coverFuture.join();
                        ShortRaster heightRaster = heightFuture.join();

                        ShortRaster result = new ShortRaster(view);

                        for (int localY = 0; localY < view.getHeight(); localY++) {
                            for (int localX = 0; localX < view.getWidth(); localX++) {
                                CoverType sampledCover = coverRaster.get(localX, localY);
                                if (sampledCover == EarthCoverTypes.WATER) {
                                    short sampledHeight = heightRaster.getShort(localX, localY);
                                    int bankType = sampledHeight <= 1 ? OCEAN : RIVER;
                                    result.setShort(localX, localY, (short) bankType);
                                }
                            }
                        }

                        return result;
                    });
        });
    }

    public static DataFuture<WaterRaster> produceWater(DataFuture<ShortRaster> bank) {
        return DataFuture.of((engine, view) -> {
            CompletableFuture<ShortRaster> bankFuture = engine.load(bank, view);
            return bankFuture.thenApply(bankRaster -> {
                WaterRaster result = new WaterRaster(view);

                for (int localZ = 0; localZ < view.getHeight(); localZ++) {
                    for (int localX = 0; localX < view.getWidth(); localX++) {
                        short waterValue = bankRaster.getShort(localX, localZ);
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
