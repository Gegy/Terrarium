package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class VanillaBiomeDecorationComposer extends VanillaDecorationComposer {
    private static final long DECORATION_SEED = 24933181514746343L;

    public VanillaBiomeDecorationComposer(World world) {
        super(world, DECORATION_SEED);
    }

    @Override
    protected void composeDecoration(IChunkGenerator generator, World world, int chunkX, int chunkZ, Biome biome) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        biome.decorate(world, this.random, new BlockPos(globalX, 0, globalZ));

        if (TerrainGen.populate(generator, world, this.random, chunkX, chunkZ, false, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
            WorldEntitySpawner.performWorldGenSpawning(world, biome, globalX + 8, globalZ + 8, 16, 16, this.random);
        }
    }
}
