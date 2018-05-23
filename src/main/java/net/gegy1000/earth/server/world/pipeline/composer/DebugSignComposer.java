package net.gegy1000.earth.server.world.pipeline.composer;

import com.google.gson.JsonObject;
import net.gegy1000.earth.server.world.pipeline.populator.DebugMap;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class DebugSignComposer implements DecorationComposer {
    private final RegionComponentType<ShortRasterTileAccess> heightComponent;

    public DebugSignComposer(RegionComponentType<ShortRasterTileAccess> heightComponent) {
        this.heightComponent = heightComponent;
    }

    @Override
    public void decorateChunk(World world, GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        ShortRasterTileAccess heightRaster = regionHandler.getCachedChunkRaster(this.heightComponent);

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

    public static class Parser implements InstanceObjectParser<DecorationComposer> {
        @Override
        public DecorationComposer parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException {
            RegionComponentType<ShortRasterTileAccess> heightComponent = valueParser.parseComponentType(objectRoot, "height_component", ShortRasterTileAccess.class);
            return new DebugSignComposer(heightComponent);
        }
    }
}
