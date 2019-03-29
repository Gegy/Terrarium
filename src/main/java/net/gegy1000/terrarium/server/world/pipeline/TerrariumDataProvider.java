package net.gegy1000.terrarium.server.world.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.FutureUtil;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.AttachedComponent;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponent;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;
import net.gegy1000.terrarium.server.world.pipeline.data.DataEngine;
import net.gegy1000.terrarium.server.world.pipeline.data.DataFuture;
import net.gegy1000.terrarium.server.world.region.GenerationRegion;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TerrariumDataProvider {
    private static final int DATA_SIZE = GenerationRegion.BUFFERED_SIZE;

    private final DataEngine engine = new DataEngine();

    private final ImmutableMap<RegionComponentType<?>, AttachedComponent<?>> attachedComponents;
    private final ImmutableList<RegionAdapter> adapters;

    private TerrariumDataProvider(
            ImmutableMap<RegionComponentType<?>, AttachedComponent<?>> attachedComponents,
            ImmutableList<RegionAdapter> adapters
    ) {
        this.attachedComponents = attachedComponents;
        this.adapters = adapters;
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("unchecked")
    public Collection<CompletableFuture<RegionComponent<?>>> loadComponents(RegionTilePos pos) {
        DataView view = new DataView(pos.getMinBufferedX(), pos.getMinBufferedZ(), DATA_SIZE, DATA_SIZE);

        Collection<CompletableFuture<RegionComponent<?>>> components = new ArrayList<>();
        for (AttachedComponent<?> component : this.attachedComponents.values()) {
            components.add((CompletableFuture<RegionComponent<?>>) (Object) component.createAndPopulate(this.engine, view));
        }

        return components;
    }

    public RegionData populateData(RegionTilePos pos) {
        Collection<CompletableFuture<RegionComponent<?>>> componentFutures = this.loadComponents(pos);

        Collection<RegionComponent<?>> components = FutureUtil.joinAll(componentFutures).join();

        Map<RegionComponentType<?>, RegionComponent<?>> componentMap = new HashMap<>();
        for (RegionComponent<?> component : components) {
            componentMap.put(component.getType(), component);
        }

        RegionData data = new RegionData(componentMap);
        this.applyAdapters(data, pos);

        return data;
    }

    @SuppressWarnings("unchecked")
    public <T extends Data> T populatePartialData(RegionComponentType<T> componentType, int x, int z, int width, int height) {
        AttachedComponent<T> attachedComponent = (AttachedComponent<T>) this.attachedComponents.get(componentType);
        if (attachedComponent == null) {
            throw new IllegalArgumentException("Cannot populate partial data tile for component that is not attached!");
        }

        DataView view = new DataView(x, z, width, height);

        RegionComponent<T> component = attachedComponent.createAndPopulate(this.engine, view).join();
        return component.getData();
    }

    public RegionData createDefaultData(int width, int height) {
        Map<RegionComponentType<?>, RegionComponent<?>> defaultComponents = new HashMap<>();
        for (RegionComponentType<?> componentType : this.attachedComponents.keySet()) {
            defaultComponents.put(componentType, componentType.createDefaultComponent(width, height));
        }

        return new RegionData(defaultComponents);
    }

    private void applyAdapters(RegionData data, RegionTilePos pos) {
        for (RegionAdapter adapter : this.adapters) {
            try {
                adapter.adapt(data, pos.getMinBufferedX(), pos.getMinBufferedZ(), DATA_SIZE, DATA_SIZE);
            } catch (Exception e) {
                Terrarium.LOGGER.warn("Failed to run adapter {}", adapter.getClass().getName(), e);
            }
        }
    }

    public ImmutableSet<RegionComponentType<?>> getAttachedComponentTypes() {
        return this.attachedComponents.keySet();
    }

    public static class Builder {
        private final Map<RegionComponentType<?>, AttachedComponent<?>> attachedComponents = new HashMap<>();
        private final List<RegionAdapter> adapters = new ArrayList<>();

        private Builder() {
        }

        public <T extends Data> Builder withComponent(RegionComponentType<T> type, DataFuture<T> data) {
            this.attachedComponents.put(type, new AttachedComponent<>(type, data));
            return this;
        }

        public Builder withAdapter(RegionAdapter adapter) {
            this.adapters.add(adapter);
            return this;
        }

        public TerrariumDataProvider build() {
            return new TerrariumDataProvider(ImmutableMap.copyOf(this.attachedComponents), ImmutableList.copyOf(this.adapters));
        }
    }
}
