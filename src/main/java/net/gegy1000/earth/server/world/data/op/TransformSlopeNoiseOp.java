package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.NoiseGeneratorOctaves;

import java.util.Random;

public final class TransformSlopeNoiseOp {
    private static final long SEED = 3819791875842730969L;

    private final NoiseGeneratorOctaves noise;
    private final double scale;

    public TransformSlopeNoiseOp(double scale) {
        this.noise = new NoiseGeneratorOctaves(new Random(SEED), 1);
        this.scale = scale;
    }

    public DataOp<UnsignedByteRaster> apply(DataOp<UnsignedByteRaster> slope) {
        if (this.scale > -1e-3 && this.scale < 1e-3) {
            return slope;
        }

        return slope.map((slopeRaster, engine, view) -> {
            double[] noise = new double[view.getWidth() * view.getHeight()];
            double frequency = (1.0 / this.scale) * 0.7;
            this.noise.generateNoiseOctaves(noise, view.getY(), view.getX(), view.getWidth(), view.getHeight(), frequency, frequency, 0.0);

            slopeRaster.transform((value, x, z) -> {
                int slopeNoise = MathHelper.floor(noise[x + z * view.getWidth()] * 35.0);
                return MathHelper.clamp(value + slopeNoise, 0, 255);
            });

            return slopeRaster;
        });
    }
}
