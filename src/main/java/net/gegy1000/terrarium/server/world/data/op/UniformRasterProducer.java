package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.ByteRaster;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class UniformRasterProducer {
    public static DataOp<ByteRaster> ofBytes(byte value) {
        return DataOp.of(view -> {
            ByteRaster result = ByteRaster.create(view);
            Arrays.fill(result.getData(), value);
            return CompletableFuture.completedFuture(result);
        });
    }

    public static DataOp<UByteRaster> ofUBytes(int value) {
        return DataOp.of(view -> {
            UByteRaster result = UByteRaster.create(view);
            Arrays.fill(result.getData(), (byte) (value & 0xFF));
            return CompletableFuture.completedFuture(result);
        });
    }

    public static DataOp<ShortRaster> ofShorts(short value) {
        return DataOp.of(view -> {
            ShortRaster result = ShortRaster.create(view);
            Arrays.fill(result.getData(), value);
            return CompletableFuture.completedFuture(result);
        });
    }

    public static <T> DataOp<ObjRaster<T>> ofObjects(T value) {
        return DataOp.of(view -> {
            ObjRaster<T> result = ObjRaster.create(value, view);
            return CompletableFuture.completedFuture(result);
        });
    }

    public static <T extends Enum<T>> DataOp<EnumRaster<T>> ofEnumVariants(T variant) {
        return DataOp.of(view -> {
            EnumRaster<T> result = EnumRaster.create(variant, view);
            return CompletableFuture.completedFuture(result);
        });
    }
}
