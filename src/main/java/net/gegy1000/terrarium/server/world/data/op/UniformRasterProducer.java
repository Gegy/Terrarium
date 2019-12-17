package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.ByteRaster;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;

import java.util.Arrays;

public class UniformRasterProducer {
    public static DataOp<ByteRaster> ofBytes(byte value) {
        return DataOp.ofSync(view -> {
            ByteRaster result = ByteRaster.create(view);
            result.fill(value);
            return result;
        });
    }

    public static DataOp<UByteRaster> ofUBytes(int value) {
        return DataOp.ofSync(view -> {
            UByteRaster result = UByteRaster.create(view);
            result.fill(value & 0xFF);
            return result;
        });
    }

    public static DataOp<ShortRaster> ofShorts(short value) {
        return DataOp.ofSync(view -> {
            ShortRaster result = ShortRaster.create(view);
            Arrays.fill(result.getData(), value);
            return result;
        });
    }

    public static <T> DataOp<ObjRaster<T>> ofObjects(T value) {
        return DataOp.ofSync(view -> ObjRaster.create(value, view));
    }

    public static <T extends Enum<T>> DataOp<EnumRaster<T>> ofEnumVariants(T variant) {
        return DataOp.ofSync(view -> EnumRaster.create(variant, view));
    }
}
