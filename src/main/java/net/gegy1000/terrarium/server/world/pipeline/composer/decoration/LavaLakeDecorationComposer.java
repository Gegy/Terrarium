package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class LavaLakeDecorationComposer extends VanillaDecorationComposer {
    private static final long DECORATION_SEED = 21052088057241959L;

    public LavaLakeDecorationComposer(World world) {
        super(world, DECORATION_SEED);
    }

    @Override
    protected void composeDecoration(IChunkGenerator generator, World world, int chunkX, int chunkZ, Biome biome) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        BlockPos pos = new BlockPos(globalX, 0, globalZ);

        if (this.random.nextInt(8) == 0) {
            if (TerrainGen.populate(generator, world, this.random, globalX, globalZ, false, PopulateChunkEvent.Populate.EventType.LAVA)) {
                int offsetX = this.random.nextInt(16) + 8;
                int offsetY = this.random.nextInt(this.random.nextInt(world.getHeight() - 8) + 8);
                int offsetZ = this.random.nextInt(16) + 8;

                if (offsetY < world.getSeaLevel() || this.random.nextInt(10) == 0) {
                    (new WorldGenLakes(Blocks.LAVA)).generate(world, this.random, pos.add(offsetX, offsetY, offsetZ));
                }
            }
        }
    }
}
