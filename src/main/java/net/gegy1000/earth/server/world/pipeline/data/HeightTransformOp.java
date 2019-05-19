package net.gegy1000.earth.server.world.pipeline.data;

import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.util.math.MathHelper;

import java.util.concurrent.CompletableFuture;

public final class HeightTransformOp {
    private final double scale;
    private final int offset;

    public HeightTransformOp(double scale, int offset) {
        this.scale = scale;
        this.offset = offset;
    }

    public DataOp<ShortRaster> apply(DataOp<ShortRaster> heights) {
        return DataOp.of((engine, view) -> {
            CompletableFuture<ShortRaster> heightFuture = engine.load(heights, view);

            return heightFuture.thenApply(heightRaster -> {
                heightRaster.transform(this::transform);
                return heightRaster;
            });
        });
    }

    private short transform(short source, int localX, int localZ) {
        int scaled = MathHelper.ceil(source * this.scale);
        return (short) Math.max(scaled + this.offset, 1);
    }
}
