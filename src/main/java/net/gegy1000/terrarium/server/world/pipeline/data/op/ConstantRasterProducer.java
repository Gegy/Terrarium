package net.gegy1000.terrarium.server.world.pipeline.data.op;

import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ByteRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class ConstantRasterProducer {
    public static DataOp<ByteRaster> bytes(byte value) {
        return DataOp.of((engine, view) -> {
            ByteRaster result = ByteRaster.create(view);
            Arrays.fill(result.getData(), value);
            return CompletableFuture.completedFuture(result);
        });
    }

    public static DataOp<UnsignedByteRaster> unsignedBytes(int value) {
        return DataOp.of((engine, view) -> {
            UnsignedByteRaster result = UnsignedByteRaster.create(view);
            Arrays.fill(result.getData(), (byte) (value & 0xFF));
            return CompletableFuture.completedFuture(result);
        });
    }

    public static DataOp<ShortRaster> shorts(short value) {
        return DataOp.of((engine, view) -> {
            ShortRaster result = ShortRaster.create(view);
            Arrays.fill(result.getData(), value);
            return CompletableFuture.completedFuture(result);
        });
    }

    public static <T> DataOp<ObjRaster<T>> objects(T value) {
        return DataOp.of((engine, view) -> {
            ObjRaster<T> result = ObjRaster.create(value, view);
            return CompletableFuture.completedFuture(result);
        });
    }
}
