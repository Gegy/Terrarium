package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

public final class NoiseOp {
    private final NoiseGeneratorSimplex noise;

    public NoiseOp(NoiseGeneratorSimplex noise) {
        this.noise = noise;
    }

    public DataOp<FloatRaster> sample(double frequency, double amplitude) {
        return DataOp.ofLazy(view -> {
            int minX = view.getMinX();
            int minY = view.getMinY();

            FloatRaster raster = FloatRaster.create(view);
            raster.transform((v, x, y) -> {
                double noiseX = (x + minX) * frequency;
                double noiseY = (y + minY) * frequency;
                double noise = this.noise.getValue(noiseX, noiseY);
                return (float) (noise * amplitude);
            });

            return raster;
        });
    }
}
