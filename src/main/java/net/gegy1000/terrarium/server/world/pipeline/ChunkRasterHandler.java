package net.gegy1000.terrarium.server.world.pipeline;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.RasterData;
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
            if (RasterData.class.isAssignableFrom(componentType.getType())) {
                this.put(chunkRastersBuilder, componentType);
            }
        }

        this.chunkRasters = chunkRastersBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private <V> void put(ImmutableMap.Builder<RegionComponentType<?>, Data<?, ?>> builder, RegionComponentType<?> componentType) {
        builder.put(componentType, new Data<>((RegionComponentType<? extends RasterData<V>>) componentType));
    }

    public void fillRastersPartial(int originX, int originZ, Collection<RegionComponentType<?>> components) {
        for (RegionComponentType<?> componentType : components) {
            Data<?, ?> data = this.chunkRasters.get(componentType);
            if (data != null) {
                data.fillRasterPartial(originX, originZ);
            }
        }
    }

    public void fillRasters(int originX, int originZ) {
        for (Map.Entry<RegionComponentType<?>, Data<?, ?>> entry : this.chunkRasters.entrySet()) {
            Data<?, ?> data = entry.getValue();
            data.fillRaster(originX, originZ);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends RasterData<V>, V> T getChunkRaster(RegionComponentType<T> componentType) {
        Data<T, V> data = (Data<T, V>) this.chunkRasters.get(componentType);
        return data.getRaster();
    }

    private class Data<T extends RasterData<V>, V> {
        private final RegionComponentType<T> componentType;
        private T raster;

        private int currentX = Integer.MIN_VALUE;
        private int currentZ = Integer.MIN_VALUE;

        private Data(RegionComponentType<T> componentType) {
            this.componentType = componentType;
            this.raster = componentType.createDefaultData(16, 16);
        }

        public void fillRasterPartial(int originX, int originZ) {
            if (this.currentX != originX || this.currentZ != originZ) {
                this.raster = ChunkRasterHandler.this.regionHandler.computePartialRaster(this.componentType, originX, originZ, 16, 16);
                this.currentX = originX;
                this.currentZ = originZ;
            }
        }

        public void fillRaster(int originX, int originZ) {
            if (this.currentX != originX || this.currentZ != originZ) {
                ChunkRasterHandler.this.regionHandler.fillRaster(this.componentType, this.raster, originX, originZ);
                this.currentX = originX;
                this.currentZ = originZ;
            }
        }

        public T getRaster() {
            return this.raster;
        }
    }
}
