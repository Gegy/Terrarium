package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.earth.server.world.pipeline.layer.DebugMap;
import net.gegy1000.terrarium.server.world.chunk.CubicPos;
import net.gegy1000.terrarium.server.world.chunk.populate.PopulateChunk;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class DebugSignDecorationComposer implements DecorationComposer {
    private final RegionComponentType<ShortRasterTile> heightComponent;

    public DebugSignDecorationComposer(RegionComponentType<ShortRasterTile> heightComponent) {
        this.heightComponent = heightComponent;
    }

    @Override
    public void composeDecoration(World world, RegionGenerationHandler regionHandler, PopulateChunk chunk) {
        CubicPos pos = chunk.getPos();
        int globalX = pos.getMinX();
        int globalZ = pos.getMinZ();

        int minY = pos.getMinY();
        int maxY = pos.getMaxY();

        ShortRasterTile heightRaster = regionHandler.getCachedChunkRaster(this.heightComponent);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                String[] signText = DebugMap.getSign(globalX + localX, globalZ + localZ);
                if (signText != null) {
                    mutablePos.setPos(localX + globalX, heightRaster.getShort(localX, localZ) + 1, localZ + globalZ);
                    if (mutablePos.getY() >= minY && mutablePos.getY() < maxY) {
                        world.setBlockState(mutablePos, Blocks.STANDING_SIGN.getDefaultState());
                        TileEntity entity = world.getTileEntity(mutablePos);
                        if (entity instanceof TileEntitySign) {
                            TileEntitySign signEntity = (TileEntitySign) entity;
                            for (int i = 0; i < signEntity.signText.length && i < signText.length; i++) {
                                signEntity.signText[i] = new TextComponentString(signText[i]);
                            }
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
