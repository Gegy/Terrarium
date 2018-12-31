package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.earth.server.world.pipeline.layer.DebugMap;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.chunk.ChunkComposer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.class_2919;
import net.minecraft.text.StringTextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class DebugSignDecorationComposer implements ChunkComposer<TerrariumGeneratorConfig> {
    private final RegionComponentType<ShortRasterTile> heightComponent;

    public DebugSignDecorationComposer(RegionComponentType<ShortRasterTile> heightComponent) {
        this.heightComponent = heightComponent;
    }

    @Override
    public void compose(ChunkGenerator<TerrariumGeneratorConfig> generator, Chunk chunk, class_2919 random, RegionGenerationHandler regionHandler) {
        ChunkPos chunkPos = chunk.getPos();
        int globalX = chunkPos.getXStart();
        int globalZ = chunkPos.getZStart();

        ShortRasterTile heightRaster = regionHandler.getCachedChunkRaster(this.heightComponent);

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                String[] signText = DebugMap.getSign(globalX + localX, globalZ + localZ);
                if (signText != null) {
                    pos.set(localX + globalX, heightRaster.getShort(localX, localZ) + 1, localZ + globalZ);
                    chunk.setBlockState(pos, Blocks.OAK_SIGN.getDefaultState(), false);
                    BlockEntity entity = chunk.getBlockEntity(pos);
                    if (entity instanceof SignBlockEntity) {
                        SignBlockEntity signEntity = (SignBlockEntity) entity;
                        for (int i = 0; i < signEntity.text.length && i < signText.length; i++) {
                            signEntity.text[i] = new StringTextComponent(signText[i]);
                        }
                    }
                }
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.heightComponent };
    }
}
