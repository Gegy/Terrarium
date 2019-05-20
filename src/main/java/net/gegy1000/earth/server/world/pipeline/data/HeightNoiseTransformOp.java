package net.gegy1000.earth.server.world.pipeline.data;

import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.world.gen.NoiseGeneratorOctaves;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public final class HeightNoiseTransformOp {
    private static final long SEED = 2816107316863058893L;

    private final NoiseGeneratorOctaves noise;
    private final double max;
    private final double scaleXZ;
    private final double scaleY;

    public HeightNoiseTransformOp(int octaveCount, double scaleXZ, double scaleY) {
        this.noise = new NoiseGeneratorOctaves(new Random(SEED), octaveCount);

        double max = 0.0;
        double scale = 1.0;
        for (int i = 0; i < octaveCount; i++) {
            max += scale * 2;
            scale /= 2.0;
        }
        this.max = max;

        this.scaleXZ = scaleXZ;
        this.scaleY = scaleY;
    }

    public DataOp<ShortRaster> apply(DataOp<ShortRaster> heights, DataOp<EnumRaster<Landform>> landforms) {
        if (this.scaleY > -1e-3 && this.scaleY < 1e-3) {
            return heights;
        }

        return DataOp.of((engine, view) -> {
            CompletableFuture<ShortRaster> heightFuture = engine.load(heights, view);
            CompletableFuture<EnumRaster<Landform>> landformFuture = engine.load(landforms, view);

            return CompletableFuture.allOf(heightFuture, landformFuture)
                    .thenApply(v -> {
                        ShortRaster heightRaster = heightFuture.join();
                        EnumRaster<Landform> landformRaster = landformFuture.join();

                        double[] noise = new double[view.getWidth() * view.getHeight()];
                        this.noise.generateNoiseOctaves(noise, view.getY(), view.getX(), view.getWidth(), view.getHeight(), this.scaleXZ, this.scaleXZ, 0.0);

                        heightRaster.transform((source, x, y) -> {
                            if (landformRaster.get(x, y) == Landform.LAND) {
                                int index = x + y * view.getWidth();
                                double offset = (noise[index] + this.max) / (this.max * 2.0) * this.scaleY * 35.0;
                                return (short) (source + offset);
                            }
                            return source;
                        });

                        return heightRaster;
                    });
        });
    }
}
