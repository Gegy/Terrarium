package net.gegy1000.terrarium.server.world.pipeline;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.RasterDataAccess;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;

import java.util.Map;

public class ChunkRasterHandler {
    private final GenerationRegionHandler regionHandler;
    private final ImmutableMap<RegionComponentType<?>, Data<?, ?>> chunkRasters;

    public ChunkRasterHandler(GenerationRegionHandler regionHandler, RegionDataSystem dataSystem) {
        this.regionHandler = regionHandler;

        ImmutableMap.Builder<RegionComponentType<?>, Data<?, ?>> chunkRastersBuilder = ImmutableMap.builder();

        ImmutableSet<RegionComponentType<?>> componentTypes = dataSystem.getAttachedComponentTypes();
        for (RegionComponentType<?> componentType : componentTypes) {
            if (RasterDataAccess.class.isAssignableFrom(componentType.getType())) {
                this.put(chunkRastersBuilder, componentType);
            }
        }

        this.chunkRasters = chunkRastersBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private <V> void put(ImmutableMap.Builder<RegionComponentType<?>, Data<?, ?>> builder, RegionComponentType<?> componentType) {
        builder.put(componentType, new Data<>((RegionComponentType<? extends RasterDataAccess<V>>) componentType));
    }

    public void fillRasters(int originX, int originZ) {
        for (Map.Entry<RegionComponentType<?>, Data<?, ?>> entry : this.chunkRasters.entrySet()) {
            Data<?, ?> data = entry.getValue();
            data.fillRaster(originX, originZ);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends RasterDataAccess<V>, V> T getChunkRaster(RegionComponentType<T> componentType) {
        Data<T, V> data = (Data<T, V>) this.chunkRasters.get(componentType);
        return data.getRaster();
    }

    private class Data<T extends RasterDataAccess<V>, V> {
        private final RegionComponentType<T> componentType;
        private final T raster;

        private Data(RegionComponentType<T> componentType) {
            this.componentType = componentType;
            this.raster = componentType.createDefaultData(16, 16);
        }

        public void fillRaster(int originX, int originZ) {
            ChunkRasterHandler.this.regionHandler.fillRaster(this.componentType, this.raster, originX, originZ, 16, 16);
        }

        public T getRaster() {
            return this.raster;
        }
    }
}
