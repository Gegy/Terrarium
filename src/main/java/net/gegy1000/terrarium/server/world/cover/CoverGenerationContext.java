package net.gegy1000.terrarium.server.world.cover;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.RasterDataAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.world.World;

public interface CoverGenerationContext {
    default <T extends RasterDataAccess<V>, V> T getRaster(TerrariumWorldData worldData, RegionComponentType<T> type) {
        return worldData.getRegionHandler().getCachedChunkRaster(type);
    }

    World getWorld();

    long getSeed();

    ShortRasterTileAccess getHeightRaster();

    CoverRasterTileAccess getCoverRaster();

    class Default implements CoverGenerationContext {
        private final World world;
        private final ShortRasterTileAccess heightRaster;
        private final CoverRasterTileAccess coverRaster;

        public Default(
                World world, TerrariumWorldData worldData,
                RegionComponentType<ShortRasterTileAccess> heightComponent,
                RegionComponentType<CoverRasterTileAccess> coverComponent
        ) {
            this.world = world;
            this.heightRaster = this.getRaster(worldData, heightComponent);
            this.coverRaster = this.getRaster(worldData, coverComponent);
        }

        @Override
        public World getWorld() {
            return this.world;
        }

        @Override
        public long getSeed() {
            return this.world.getWorldInfo().getSeed();
        }

        @Override
        public ShortRasterTileAccess getHeightRaster() {
            return this.heightRaster;
        }

        @Override
        public CoverRasterTileAccess getCoverRaster() {
            return this.coverRaster;
        }

        public static class Parser implements InstanceObjectParser<CoverGenerationContext> {
            @Override
            public CoverGenerationContext parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException {
                RegionComponentType<ShortRasterTileAccess> heightComponent = valueParser.parseComponentType(objectRoot, "height_component", ShortRasterTileAccess.class);
                RegionComponentType<CoverRasterTileAccess> coverComponent = valueParser.parseComponentType(objectRoot, "cover_component", CoverRasterTileAccess.class);

                return new Default(world, worldData, heightComponent, coverComponent);
            }
        }
    }
}
