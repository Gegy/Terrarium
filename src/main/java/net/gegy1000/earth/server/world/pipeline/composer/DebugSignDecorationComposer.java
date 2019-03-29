package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.pipeline.data.DebugMap;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class DebugSignDecorationComposer implements DecorationComposer {
    private final RegionComponentType<ShortRaster> heightComponent;

    public DebugSignDecorationComposer(RegionComponentType<ShortRaster> heightComponent) {
        this.heightComponent = heightComponent;
    }

    @Override
    public void composeDecoration(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPopulationWriter writer) {
        int globalX = pos.getMinX();
        int globalZ = pos.getMinZ();

        int minY = pos.getMinY();
        int maxY = pos.getMaxY();

        ShortRaster heightRaster = regionHandler.getCachedChunkRaster(this.heightComponent);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                String[] signText = DebugMap.getSign(globalX + localX, globalZ + localZ);
                if (signText != null) {
                    mutablePos.setPos(localX + globalX, heightRaster.getShort(localX, localZ) + 1, localZ + globalZ);
                    if (mutablePos.getY() >= minY && mutablePos.getY() <= maxY) {
                        writer.set(mutablePos, Blocks.STANDING_SIGN.getDefaultState());
                        TileEntity entity = writer.getGlobal().getTileEntity(mutablePos);
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
