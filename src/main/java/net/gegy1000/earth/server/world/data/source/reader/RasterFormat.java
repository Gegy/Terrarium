package net.gegy1000.earth.server.world.data.source.reader;

import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.ByteRaster;
import net.gegy1000.terrarium.server.world.data.raster.IntegerRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

class RasterFormat<T extends IntegerRaster<?>> {
    static final RasterFormat<UByteRaster> UBYTE = new RasterFormat<>(UByteRaster.class, UByteRaster::create, (input, width, height) -> {
        byte[] bytes = new byte[width * height];
        new DataInputStream(input).readFully(bytes);
        return UByteRaster.wrap(bytes, width, height);
    });

    static final RasterFormat<ByteRaster> BYTE = new RasterFormat<>(ByteRaster.class, ByteRaster::create, (input, width, height) -> {
        byte[] bytes = new byte[width * height];
        new DataInputStream(input).readFully(bytes);
        return ByteRaster.wrap(bytes, width, height);
    });

    static final RasterFormat<ShortRaster> SHORT = new RasterFormat<>(ShortRaster.class, ShortRaster::create, (input, width, height) -> {
        DataInputStream dataIn = new DataInputStream(input);
        short[] values = new short[width * height];
        for (int i = 0; i < values.length; i++) {
            values[i] = dataIn.readShort();
        }
        return ShortRaster.wrap(values, width, height);
    });

    private final Class<T> type;
    private final Function<DataView, T> constructor;
    private final RasterReader<T> reader;

    private RasterFormat(Class<T> type, Function<DataView, T> constructor, RasterReader<T> reader) {
        this.type = type;
        this.constructor = constructor;
        this.reader = reader;
    }

    T create(DataView view) {
        return this.constructor.apply(view);
    }

    T read(InputStream input, int width, int height) throws IOException {
        return this.reader.read(input, width, height);
    }

    @SuppressWarnings("unchecked")
    <R extends IntegerRaster<?>> Optional<RasterFormat<R>> tryCast(Class<R> type) {
        if (!type.isAssignableFrom(this.type)) return Optional.empty();
        return Optional.of((RasterFormat<R>) this);
    }

    static Optional<RasterFormat<?>> byId(int id) {
        switch (id) {
            case 0: return Optional.of(RasterFormat.UBYTE);
            case 1: return Optional.of(RasterFormat.BYTE);
            case 2: return Optional.of(RasterFormat.SHORT);
            default: return Optional.empty();
        }
    }

    interface RasterReader<T extends IntegerRaster<?>> {
        T read(InputStream input, int width, int height) throws IOException;
    }
}
