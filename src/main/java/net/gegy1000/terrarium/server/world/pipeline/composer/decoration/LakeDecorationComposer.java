package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.terrarium.server.world.chunk.CubicPos;
import net.gegy1000.terrarium.server.world.chunk.populate.PopulateChunk;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class LakeDecorationComposer extends VanillaDecorationComposer {
    private static final long DECORATION_SEED = 1576583677480695379L;

    private final WorldGenLakes generator = new WorldGenLakes(Blocks.WATER);

    public LakeDecorationComposer(World world) {
        super(world, DECORATION_SEED);
    }

    @Override
    protected void composeDecoration(World world, PopulateChunk chunk, Biome biome) {
        CubicPos pos = chunk.getPos();
        BlockPos origin = pos.getCenter();

        if (biome != Biomes.DESERT && this.horizontalRandom.nextInt(16) == 0) {
            // TODO: Cube alternative (also cannot pass null!)
            if (TerrainGen.populate(null, world, this.horizontalRandom, pos.getX(), pos.getZ(), false, PopulateChunkEvent.Populate.EventType.LAKE)) {
                int offsetX = this.random.nextInt(16);
                int offsetY = this.random.nextInt(16);
                int offsetZ = this.random.nextInt(16);
                this.generator.generate(world, this.random, origin.add(offsetX, offsetY, offsetZ));
            }
        }
    }
}
