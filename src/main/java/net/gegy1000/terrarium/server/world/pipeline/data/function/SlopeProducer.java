package net.gegy1000.terrarium.server.world.pipeline.data.function;

import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.DataFuture;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;
import net.minecraft.util.math.MathHelper;

public final class SlopeProducer {
    public static DataFuture<UnsignedByteRaster> produce(DataFuture<ShortRaster> heights) {
        return DataFuture.of((engine, view) -> {
            DataView sourceView = view.grow(1, 1, 1, 1);
            return engine.load(heights, sourceView)
                    .thenApply(source -> {
                        UnsignedByteRaster result = new UnsignedByteRaster(view);
                        for (int localY = 0; localY < view.getHeight(); localY++) {
                            for (int localX = 0; localX < view.getWidth(); localX++) {
                                int slope = computeSlope(source, localY, localX);
                                result.setByte(localX, localY, MathHelper.clamp(slope, 0, 255));
                            }
                        }
                        return result;
                    });
        });
    }

    private static int computeSlope(ShortRaster source, int localY, int localX) {
        int sourceX = localX + 1;
        int sourceY = localY + 1;
        short current = source.getShort(sourceX, sourceY);

        int topLeft = Math.abs(current - source.getShort(sourceX - 1, sourceY - 1));
        int topRight = Math.abs(current - source.getShort(sourceX + 1, sourceY - 1));
        int bottomLeft = Math.abs(current - source.getShort(sourceX - 1, sourceY + 1));
        int bottomRight = Math.abs(current - source.getShort(sourceX + 1, sourceY + 1));

        return (topLeft + topRight + bottomLeft + bottomRight) / 2;
    }
}
