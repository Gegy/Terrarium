package net.gegy1000.earth.server.world.data.op;

import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public final class AddNoiseOp {
    private final NoiseGeneratorPerlin noise;
    private final double frequency;
    private final double amplitude;

    public AddNoiseOp(NoiseGeneratorPerlin noise, double frequency, double amplitude) {
        this.noise = noise;
        this.frequency = frequency;
        this.amplitude = amplitude;
    }

    public DataOp<FloatRaster> applyFloats(DataOp<FloatRaster> sourceOp) {
        return sourceOp.map((raster, view) -> {
            int minX = view.getMinX();
            int minY = view.getMinY();
            raster.transform((source, x, y) -> {
                double noiseX = (x + minX) * this.frequency;
                double noiseY = (y + minY) * this.frequency;
                double noise = this.noise.getValue(noiseX, noiseY);
                return source + (float) (noise * this.amplitude);
            });
            return raster;
        });
    }
}
