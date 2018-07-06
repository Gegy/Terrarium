package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.earth.server.world.pipeline.layer.DebugMap;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
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
    public void composeDecoration(World world, GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        ShortRasterTile heightRaster = regionHandler.getCachedChunkRaster(this.heightComponent);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                String[] signText = DebugMap.getSign(globalX + localX, globalZ + localZ);
                if (signText != null) {
                    pos.setPos(localX + globalX, heightRaster.getShort(localX, localZ) + 1, localZ + globalZ);
                    world.setBlockState(pos, Blocks.STANDING_SIGN.getDefaultState());
                    TileEntity entity = world.getTileEntity(pos);
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
