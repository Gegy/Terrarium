package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.terrarium.server.world.chunk.CubicPos;
import net.gegy1000.terrarium.server.world.chunk.populate.PopulateChunk;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class LavaLakeDecorationComposer extends VanillaDecorationComposer {
    private static final long DECORATION_SEED = 21052088057241959L;

    public LavaLakeDecorationComposer(World world) {
        super(world, DECORATION_SEED);
    }

    @Override
    protected void composeDecoration(World world, PopulateChunk chunk, Biome biome) {
        CubicPos pos = chunk.getPos();
        BlockPos origin = pos.getCenter();

        if (this.horizontalRandom.nextInt(8) == 0) {
            int offsetY = this.random.nextInt(this.random.nextInt(world.getHeight() - 8) + 8);

            if (offsetY >= pos.getMinY() && offsetY <= pos.getMaxY()) {
                // TODO
                if (TerrainGen.populate(null, world, this.horizontalRandom, pos.getX(), pos.getZ(), false, PopulateChunkEvent.Populate.EventType.LAVA)) {
                    int offsetX = this.random.nextInt(16);
                    int offsetZ = this.random.nextInt(16);

                    if (offsetY < world.getSeaLevel() || this.random.nextInt(10) == 0) {
                        (new WorldGenLakes(Blocks.LAVA)).generate(world, this.random, origin.add(offsetX, offsetY, offsetZ));
                    }
                }
            }
        }
    }
}
