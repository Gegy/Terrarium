package net.gegy1000.earth.server.world.data.source.reader;

import net.gegy1000.terrarium.server.util.Profiler;
import net.gegy1000.terrarium.server.util.ThreadedProfiler;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.IntegerRaster;
import net.gegy1000.terrarium.server.world.data.raster.Raster;
import org.tukaani.xz.SingleXZInputStream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class TerrariumRasterReader {
    private static final String SIGNATURE = "TERRARIUM/RASTER";

    public static <T extends IntegerRaster<?>> T read(InputStream input, RasterFormat<T> rasterType) throws IOException {
        Profiler profiler = ThreadedProfiler.get();
        try (Profiler.Handle read = profiler.push("read_terrarium_raster")) {
            DataInputStream dataIn = new DataInputStream(input);

            byte[] signature = new byte[SIGNATURE.length()];
            dataIn.readFully(signature);

            if (!Arrays.equals(signature, SIGNATURE.getBytes())) throw new IOException("Invalid signature");

            int version = dataIn.readUnsignedByte();
            if (version == 0) {
                return readV0(dataIn, rasterType);
            } else {
                throw new IOException("Unknown data version " + version);
            }
        }
    }

    private static <T extends IntegerRaster<?>> T readV0(InputStream input, RasterFormat<T> format) throws IOException {
        DataInputStream dataIn = new DataInputStream(input);
        int width = dataIn.readInt();
        int height = dataIn.readInt();

        RasterFormat<?> dataFormat = RasterFormat.byId(dataIn.readUnsignedByte());
        if (dataFormat != format) {
            throw new IOException("Expected raster of type " + format);
        }

        T raster = format.create(DataView.of(width, height));

        while (dataIn.available() > 0) {
            int chunkLength = dataIn.readInt();
            byte[] chunkBytes = new byte[chunkLength];
            dataIn.readFully(chunkBytes);

            readChunkV0(new ByteArrayInputStream(chunkBytes), raster, format);
        }

        return raster;
    }

    private static <T extends IntegerRaster<?>> void readChunkV0(
            InputStream input,
            T dstRaster,
            RasterFormat<T> rasterFormat
    ) throws IOException {
        DataInputStream dataIn = new DataInputStream(input);
        int chunkX = dataIn.readInt();
        int chunkY = dataIn.readInt();
        int chunkWidth = dataIn.readInt();
        int chunkHeight = dataIn.readInt();
        RasterFilter filter = RasterFilter.byId(dataIn.readUnsignedByte());

        DataView srcView = DataView.of(chunkX, chunkY, chunkWidth, chunkHeight);
        DataView dstView = DataView.of(dstRaster.width(), dstRaster.height());

        T rawRaster = rasterFormat.read(new SingleXZInputStream(input), chunkWidth, chunkHeight);
        T filteredRaster = rasterFormat.create(srcView);
        filter.apply(rawRaster, filteredRaster);

        Raster.rasterCopy(filteredRaster, srcView, dstRaster, dstView);
    }
}
