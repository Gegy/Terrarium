package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.terrarium.server.world.chunk.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.Random;

public abstract class VanillaDecorationComposer implements DecorationComposer {
    private static final BlockPos DECORATION_CENTER = new BlockPos(16, 0, 16);

    protected final Random random;
    private final PseudoRandomMap randomMap;

    protected VanillaDecorationComposer(World world, long seed) {
        this.randomMap = new PseudoRandomMap(world.getWorldInfo().getSeed(), seed);
        this.random = new Random(0);
    }

    @Override
    public final void composeDecoration(IChunkGenerator generator, World world, RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        this.randomMap.initPosSeed(globalX, globalZ);
        this.random.setSeed(this.randomMap.next());

        Biome biome = world.getChunk(chunkX, chunkZ).getBiome(DECORATION_CENTER, world.getBiomeProvider());
        this.composeDecoration(generator, world, chunkX, chunkZ, biome);
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }

    protected abstract void composeDecoration(IChunkGenerator generator, World world, int chunkX, int chunkZ, Biome biome);
}
