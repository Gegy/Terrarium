package net.gegy1000.terrarium.server.world.pipeline.composer.chunk;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.BlockState;
import net.minecraft.class_2919;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class BedrockComposer<C extends TerrariumGeneratorConfig> implements ChunkComposer<C> {
    private final BlockState block;
    private final int scatterRange;

    public BedrockComposer(BlockState block, int scatterRange) {
        this.block = block;
        this.scatterRange = scatterRange;
    }

    @Override
    public void compose(ChunkGenerator<C> generator, Chunk chunk, class_2919 random, RegionGenerationHandler regionHandler) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        BlockPos min = chunk.getPos().method_8323();
        BlockPos max = min.add(16, 0, 16);

        for (BlockPos pos : BlockPos.iterateBoxPositionsMutable(min, max)) {
            for (int y = this.scatterRange + 1; y >= 0; y--) {
                if (y <= random.nextInt(this.scatterRange)) {
                    mutablePos.set(pos.getX(), y, pos.getZ());
                    chunk.setBlockState(mutablePos, this.block, false);
                }
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
