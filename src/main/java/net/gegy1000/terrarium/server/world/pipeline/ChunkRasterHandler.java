package net.gegy1000.terrarium.server.world.pipeline;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.RasterDataAccess;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;

import java.util.Collection;
import java.util.Map;

public class ChunkRasterHandler {
    private final RegionGenerationHandler regionHandler;
    private final ImmutableMap<RegionComponentType<?>, Data<?, ?>> chunkRasters;

    public ChunkRasterHandler(RegionGenerationHandler regionHandler, TerrariumDataProvider dataSystem) {
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

    public void fillRasters(int originX, int originZ, Collection<RegionComponentType<?>> components) {
        for (RegionComponentType<?> componentType : components) {
            Data<?, ?> data = this.chunkRasters.get(componentType);
            if (data != null) {
                data.fillRaster(originX, originZ, true);
            }
        }
    }

    public void fillRasters(int originX, int originZ) {
        for (Map.Entry<RegionComponentType<?>, Data<?, ?>> entry : this.chunkRasters.entrySet()) {
            Data<?, ?> data = entry.getValue();
            data.fillRaster(originX, originZ, false);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends RasterDataAccess<V>, V> T getChunkRaster(RegionComponentType<T> componentType) {
        Data<T, V> data = (Data<T, V>) this.chunkRasters.get(componentType);
        return data.getRaster();
    }

    private class Data<T extends RasterDataAccess<V>, V> {
        private final RegionComponentType<T> componentType;
        private T raster;

        private int currentX = Integer.MIN_VALUE;
        private int currentZ = Integer.MIN_VALUE;

        private Data(RegionComponentType<T> componentType) {
            this.componentType = componentType;
            this.raster = componentType.createDefaultData(16, 16);
        }

        public void fillRaster(int originX, int originZ, boolean allowPartial) {
            if (this.currentX != originX || this.currentZ != originZ) {
                this.raster = ChunkRasterHandler.this.regionHandler.fillRaster(this.componentType, this.raster, originX, originZ, 16, 16, allowPartial);
                this.currentX = originX;
                this.currentZ = originZ;
            }
        }

        public T getRaster() {
            return this.raster;
        }
    }
}
