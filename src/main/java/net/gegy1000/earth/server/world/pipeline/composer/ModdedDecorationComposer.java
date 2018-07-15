package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.earth.server.EarthDecorationEventHandler;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.List;
import java.util.Random;

public class ModdedDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 6926778772467445428L;

    private final Random random;
    private final long worldSeed;

    public ModdedDecorationComposer(World world) {
        this.worldSeed = world.getWorldInfo().getSeed();
        this.random = new Random(this.worldSeed);
    }

    @Override
    public void composeDecoration(IChunkGenerator generator, World world, RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
        List<IWorldGenerator> capturedGenerators = EarthDecorationEventHandler.capturedGenerators;
        if (capturedGenerators != null && !capturedGenerators.isEmpty()) {
            IChunkProvider chunkProvider = world.getChunkProvider();

            this.random.setSeed(this.worldSeed);
            long seedX = this.random.nextLong() >> 2 + 1L;
            long seedZ = this.random.nextLong() >> 2 + 1L;

            long chunkSeed = (seedX * chunkX + seedZ * chunkZ) ^ DECORATION_SEED;
            for (IWorldGenerator worldGenerator : capturedGenerators) {
                this.random.setSeed(chunkSeed);
                worldGenerator.generate(this.random, chunkX, chunkZ, world, generator, chunkProvider);
            }
        }
    }
}
