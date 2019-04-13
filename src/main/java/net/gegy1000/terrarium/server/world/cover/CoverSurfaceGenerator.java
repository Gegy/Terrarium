package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;

import java.util.Random;

public abstract class CoverSurfaceGenerator<T extends CoverGenerationContext> extends CoverGenerator<T> {
    protected CoverSurfaceGenerator(T context, CoverType<T> coverType) {
        super(context, coverType);
    }

    public void decorate(CubicPos chunkPos, ChunkPrimeWriter writer, Random random) {
    }

    public static class Inherit<T extends CoverGenerationContext> extends CoverSurfaceGenerator<T> {
        public Inherit(T context, CoverType<T> coverType) {
            super(context, coverType);
        }
    }
}
