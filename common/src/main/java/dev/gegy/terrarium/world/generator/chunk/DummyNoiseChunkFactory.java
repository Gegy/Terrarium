package dev.gegy.terrarium.world.generator.chunk;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;

public class DummyNoiseChunkFactory {
    private static final NoiseGeneratorSettings DUMMY_NOISE_SETTINGS = NoiseGeneratorSettings.dummy();
    private static final Beardifier DUMMY_BEARDIFIER = new Beardifier(
            ObjectLists.<Beardifier.Rigid>emptyList().listIterator(),
            ObjectLists.<JigsawJunction>emptyList().listIterator()
    );

    private static final Aquifer.FluidStatus DUMMY_FLUID_STATUS = new Aquifer.FluidStatus(Integer.MIN_VALUE, Blocks.AIR.defaultBlockState());
    private static final Aquifer.FluidPicker DUMMY_FLUID_PICKER = (x, y, z) -> DUMMY_FLUID_STATUS;

    public static NoiseChunk getOrCreate(final ChunkAccess chunk, final RandomState randomState, final SurfaceSampler surfaceSampler) {
        return chunk.getOrCreateNoiseChunk(c -> create(c, randomState, surfaceSampler));
    }

    private static NoiseChunk create(final ChunkAccess chunk, final RandomState randomState, final SurfaceSampler surfaceSampler) {
        final NoiseSettings noiseSettings = DUMMY_NOISE_SETTINGS.noiseSettings().clampToHeightAccessor(chunk);
        final ChunkPos chunkPos = chunk.getPos();
        return new NoiseChunk(1, randomState, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), noiseSettings, DUMMY_BEARDIFIER, DUMMY_NOISE_SETTINGS, DUMMY_FLUID_PICKER, Blender.empty()) {
            @Override
            public int preliminarySurfaceLevel(final int x, final int z) {
                return surfaceSampler.getSurfaceY(x, z);
            }
        };
    }

    public interface SurfaceSampler {
        int getSurfaceY(int x, int z);
    }
}
