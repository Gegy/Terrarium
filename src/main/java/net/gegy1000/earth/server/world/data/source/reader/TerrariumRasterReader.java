package net.gegy1000.earth.server.world.data.source.reader;

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

    public static <T extends IntegerRaster<?>> T read(InputStream input, Class<T> rasterType) throws IOException {
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

    private static <T extends IntegerRaster<?>> T readV0(InputStream input, Class<T> expectedRasterType) throws IOException {
        DataInputStream dataIn = new DataInputStream(input);
        int width = dataIn.readInt();
        int height = dataIn.readInt();

        RasterFormat<T> byteFormat = RasterFormat.byId(dataIn.readUnsignedByte())
                .flatMap(format -> format.tryCast(expectedRasterType))
                .orElseThrow(() -> new IOException("Expected raster of type " + expectedRasterType));

        T raster = byteFormat.create(DataView.rect(width, height));

        while (dataIn.available() > 0) {
            int chunkLength = dataIn.readInt();
            byte[] chunkBytes = new byte[chunkLength];
            dataIn.readFully(chunkBytes);

            readChunkV0(new ByteArrayInputStream(chunkBytes), raster, byteFormat);
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

        DataView srcView = DataView.rect(chunkX, chunkY, chunkWidth, chunkHeight);
        DataView dstView = dstRaster.asView();

        T rawRaster = rasterFormat.read(new SingleXZInputStream(input), chunkWidth, chunkHeight);
        T filteredRaster = rasterFormat.create(srcView);
        filter.apply(rawRaster, filteredRaster);

        Raster.rasterCopy(filteredRaster, srcView, dstRaster, dstView);
    }
}
