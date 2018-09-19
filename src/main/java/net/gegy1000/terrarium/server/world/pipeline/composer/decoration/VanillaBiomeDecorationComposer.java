package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.terrarium.server.world.chunk.CubicPos;
import net.gegy1000.terrarium.server.world.chunk.populate.PopulateChunk;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class VanillaBiomeDecorationComposer extends VanillaDecorationComposer {
    private static final long DECORATION_SEED = 24933181514746343L;

    public VanillaBiomeDecorationComposer(World world) {
        super(world, DECORATION_SEED);
    }

    @Override
    protected void composeDecoration(World world, PopulateChunk chunk, Biome biome) {
        CubicPos pos = chunk.getPos();
        int globalX = pos.getMinX();
        int globalZ = pos.getMinZ();

        // TODO: This can't work
//        biome.decorate(world, this.horizontalRandom, new BlockPos(globalX, 0, globalZ));
//
//        // TODO
//        if (TerrainGen.populate(null, world, this.horizontalRandom, pos.getX(), pos.getZ(), false, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
//            WorldEntitySpawner.performWorldGenSpawning(world, biome, globalX + 8, globalZ + 8, 16, 16, this.horizontalRandom);
//        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
