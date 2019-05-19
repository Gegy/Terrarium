package net.gegy1000.earth.server.world.pipeline.data;

import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
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

    public DataOp<ShortRaster> apply(DataOp<ShortRaster> heights, DataOp<WaterRaster> water) {
        if (this.scaleY > -1e-3 && this.scaleY < 1e-3) {
            return heights;
        }

        return DataOp.of((engine, view) -> {
            CompletableFuture<ShortRaster> heightFuture = engine.load(heights, view);
            CompletableFuture<WaterRaster> waterFuture = engine.load(water, view);

            return CompletableFuture.allOf(heightFuture, waterFuture)
                    .thenApply(v -> {
                        ShortRaster heightRaster = heightFuture.join();
                        WaterRaster waterRaster = waterFuture.join();

                        double[] noise = new double[view.getWidth() * view.getHeight()];
                        this.noise.generateNoiseOctaves(noise, view.getY(), view.getX(), view.getWidth(), view.getHeight(), this.scaleXZ, this.scaleXZ, 0.0);

                        short[] heightBuffer = heightRaster.getData();
                        short[] waterBuffer = waterRaster.getData();

                        for (int i = 0; i < noise.length; i++) {
                            if (WaterRaster.isLand(waterBuffer[i])) {
                                heightBuffer[i] += (noise[i] + this.max) / (this.max * 2.0) * this.scaleY * 35.0;
                            }
                        }

                        return heightRaster;
                    });
        });
    }
}
