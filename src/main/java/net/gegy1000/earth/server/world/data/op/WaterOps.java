package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.justnow.future.Future;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.BitRaster;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;

public final class WaterOps {
    public static DataOp<EnumRaster<Landform>> applyWaterMask(DataOp<EnumRaster<Landform>> landforms, DataOp<BitRaster> ocean) {
        return DataOp.of((view, executor) -> {
            return Future.map2(landforms.apply(view, executor), ocean.apply(view, executor), (landformOption, oceanOption) -> {
                return landformOption.map(landformRaster -> {
                    if (oceanOption.isPresent()) {
                        BitRaster oceanRaster = oceanOption.get();
                        landformRaster.transform((source, x, y) -> {
                            if (oceanRaster.get(x, y)) {
                                return Landform.SEA;
                            } else if (source.isWater()) {
                                return Landform.LAND;
                            }
                            return source;
                        });
                    }
                    return landformRaster;
                });
            });
        });
    }

    public static DataOp<ShortRaster> produceWaterLevel(DataOp<ShortRaster> elevation, DataOp<EnumRaster<Landform>> landforms, int seaLevel) {
        return DataOp.map2(elevation, landforms, (view, heightRaster, landformRaster) -> {
            ShortRaster waterLevelRaster = ShortRaster.create(view);

            landformRaster.iterate((landform, x, y) -> {
                if (landform == Landform.SEA) {
                    waterLevelRaster.set(x, y, (short) seaLevel);
                } else if (landform == Landform.LAKE_OR_RIVER) {
                    waterLevelRaster.set(x, y, heightRaster.get(x, y));
                } else {
                    waterLevelRaster.set(x, y, Short.MIN_VALUE);
                }
            });

            return waterLevelRaster;
        });
    }

    public static DataOp<ShortRaster> applyToHeight(DataOp<ShortRaster> elevation, DataOp<EnumRaster<Landform>> landforms, int seaLevel) {
        return DataOp.map2(elevation, landforms, (view, heightRaster, landformRaster) -> {
            heightRaster.transform((source, x, y) -> {
                Landform landform = landformRaster.get(x, y);
                if (landform == Landform.SEA) {
                    return (short) Math.min(source, seaLevel - 1);
                } else if (landform == Landform.LAND && source < seaLevel) {
                    return (short) seaLevel;
                } else if (landform == Landform.LAKE_OR_RIVER) {
                    return (short) (source - 1);
                }
                return source;
            });

            return heightRaster;
        });
    }

    public static DataOp<EnumRaster<Cover>> applyToCover(DataOp<EnumRaster<Cover>> cover, DataOp<EnumRaster<Landform>> landforms) {
        return DataOp.map2(cover, landforms, (view, coverRaster, landformRaster) -> {
            coverRaster.transform((source, x, y) -> {
                Landform landform = landformRaster.get(x, y);
                if (landform.isWater()) {
                    return Cover.WATER;
                } else if (source == Cover.WATER) {
                    return Cover.NO;
                }
                return source;
            });

            return coverRaster;
        });
    }
}
