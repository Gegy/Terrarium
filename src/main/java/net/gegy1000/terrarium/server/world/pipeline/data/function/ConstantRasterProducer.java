package net.gegy1000.terrarium.server.world.pipeline.data.function;

import net.gegy1000.terrarium.server.world.pipeline.data.DataFuture;
import net.gegy1000.terrarium.server.world.pipeline.data.RasterConstructor;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ByteRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.RasterData;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class ConstantRasterProducer {
    public static DataFuture<ByteRaster> byteRaster(byte value) {
        return DataFuture.of((engine, view) -> {
            ByteRaster result = new ByteRaster(view);
            Arrays.fill(result.getByteData(), value);
            return CompletableFuture.completedFuture(result);
        });
    }

    public static DataFuture<UnsignedByteRaster> unsignedByteRaster(int value) {
        return DataFuture.of((engine, view) -> {
            UnsignedByteRaster result = new UnsignedByteRaster(view);
            Arrays.fill(result.getByteData(), (byte) (value & 0xFF));
            return CompletableFuture.completedFuture(result);
        });
    }

    public static DataFuture<ShortRaster> shortRaster(short value) {
        return DataFuture.of((engine, view) -> {
            ShortRaster result = new ShortRaster(view);
            Arrays.fill(result.getShortData(), value);
            return CompletableFuture.completedFuture(result);
        });
    }

    public static <T extends RasterData<V>, V> DataFuture<T> objectRaster(RasterConstructor<T> constructor, V value) {
        return DataFuture.of((engine, view) -> {
            T result = constructor.construct(view);
            Arrays.fill(result.getData(), value);
            return CompletableFuture.completedFuture(result);
        });
    }
}
