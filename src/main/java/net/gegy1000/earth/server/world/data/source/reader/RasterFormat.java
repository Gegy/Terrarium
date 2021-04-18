package net.gegy1000.earth.server.world.data.source.reader;

import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.ByteRaster;
import net.gegy1000.terrarium.server.world.data.raster.IntegerRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

public final class RasterFormat<T extends IntegerRaster<?>> {
    public static final RasterFormat<UByteRaster> UBYTE = new RasterFormat<>(UByteRaster::create, (input, width, height) -> {
        byte[] bytes = new byte[width * height];
        new DataInputStream(input).readFully(bytes);
        return UByteRaster.wrap(bytes, width, height);
    });

    public static final RasterFormat<ByteRaster> BYTE = new RasterFormat<>(ByteRaster::create, (input, width, height) -> {
        byte[] bytes = new byte[width * height];
        new DataInputStream(input).readFully(bytes);
        return ByteRaster.wrap(bytes, width, height);
    });

    public static final RasterFormat<ShortRaster> SHORT = new RasterFormat<>(ShortRaster::create, (input, width, height) -> {
        DataInputStream dataIn = new DataInputStream(input);
        short[] values = new short[width * height];
        for (int i = 0; i < values.length; i++) {
            values[i] = dataIn.readShort();
        }
        return ShortRaster.wrap(values, width, height);
    });

    private final Function<DataView, T> constructor;
    private final Reader<T> reader;

    private RasterFormat(Function<DataView, T> constructor, Reader<T> reader) {
        this.constructor = constructor;
        this.reader = reader;
    }

    T create(DataView view) {
        return this.constructor.apply(view);
    }

    T read(InputStream input, int width, int height) throws IOException {
        return this.reader.read(input, width, height);
    }

    @Nullable
    static RasterFormat<?> byId(int id) {
        switch (id) {
            case 0: return RasterFormat.UBYTE;
            case 1: return RasterFormat.BYTE;
            case 2: return RasterFormat.SHORT;
            default: return null;
        }
    }

    interface Reader<T extends IntegerRaster<?>> {
        T read(InputStream input, int width, int height) throws IOException;
    }
}
