package net.gegy1000.terrarium.server.map.system;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.RegionData;
import net.gegy1000.terrarium.server.map.RegionTilePos;
import net.gegy1000.terrarium.server.map.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.map.system.component.RegionComponent;
import net.gegy1000.terrarium.server.map.system.component.RegionComponentType;
import net.gegy1000.terrarium.server.map.system.populator.DependentRegionPopulator;
import net.gegy1000.terrarium.server.map.system.populator.RegionPopulator;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionPopulationSystem {
    private final EarthGenerationSettings settings;

    private final ImmutableMap<RegionComponentType<?>, AttachedComponentType<?>> attachedComponents;
    private final ImmutableList<AttachedDependentComponentType<?, ?>> attachedDependentComponents;
    private final ImmutableList<RegionAdapter> adapters;

    private RegionPopulationSystem(
            EarthGenerationSettings settings,
            ImmutableMap<RegionComponentType<?>, AttachedComponentType<?>> attachedComponents,
            ImmutableList<AttachedDependentComponentType<?, ?>> attachedDependentComponents,
            ImmutableList<RegionAdapter> adapters
    ) {
        this.settings = settings;
        this.attachedComponents = attachedComponents;
        this.attachedDependentComponents = attachedDependentComponents;
        this.adapters = adapters;
    }

    public static Builder builder(EarthGenerationSettings settings) {
        return new Builder(settings);
    }

    public RegionData populateData(RegionTilePos pos, Coordinate regionSize, int width, int height) {
        Map<RegionComponentType<?>, RegionComponent<?>> populatedComponents = new HashMap<>();
        for (AttachedComponentType<?> attachedComponent : this.attachedComponents.values()) {
            RegionComponent<?> component = attachedComponent.createAndPopulate(this.settings, pos, regionSize, width, height);
            populatedComponents.put(attachedComponent.type, component);
        }
        for (AttachedDependentComponentType<?, ?> attachedDependentComponent : this.attachedDependentComponents) {
            RegionComponent<?> component = attachedDependentComponent.createAndPopulate(populatedComponents, this.settings, pos, regionSize, width, height);
            populatedComponents.put(attachedDependentComponent.type, component);
        }
        RegionData data = new RegionData(populatedComponents);
        this.applyAdapters(data, pos, width, height);
        return data;
    }

    private void applyAdapters(RegionData data, RegionTilePos pos, int width, int height) {
        for (RegionAdapter adapter : this.adapters) {
            try {
                adapter.adapt(this.settings, data, pos.getMinX(), pos.getMinZ(), width, height);
            } catch (Exception e) {
                Terrarium.LOGGER.warn("Failed to run adapter {}", adapter.getClass().getName(), e);
            }
        }
    }

    public static class Builder {
        private final EarthGenerationSettings settings;
        private final Map<RegionComponentType<?>, AttachedComponentType<?>> attachedComponents = new HashMap<>();
        private final List<AttachedDependentComponentType<?, ?>> attachedDependentComponents = new ArrayList<>();
        private final List<RegionAdapter> adapters = new ArrayList<>();

        private Builder(EarthGenerationSettings settings) {
            this.settings = settings;
        }

        public <T> Builder withComponent(RegionComponentType<T> type, RegionPopulator<T> populator) {
            this.attachedComponents.put(type, new AttachedComponentType<>(type, populator));
            return this;
        }

        public <T, V> Builder withDependentComponent(RegionComponentType<T> type, RegionComponentType<V> dependentType, DependentRegionPopulator<T, V> populator) {
            this.attachedDependentComponents.add(new AttachedDependentComponentType<>(type, dependentType, populator));
            return this;
        }

        public Builder withAdapter(RegionAdapter adapter) {
            this.adapters.add(adapter);
            return this;
        }

        public RegionPopulationSystem build() {
            return new RegionPopulationSystem(this.settings, ImmutableMap.copyOf(this.attachedComponents), ImmutableList.copyOf(this.attachedDependentComponents), ImmutableList.copyOf(this.adapters));
        }
    }

    private static class AttachedComponentType<T> {
        private final RegionComponentType<T> type;
        private final RegionPopulator<T> populator;

        private AttachedComponentType(RegionComponentType<T> type, RegionPopulator<T> populator) {
            this.type = type;
            this.populator = populator;
        }

        public RegionComponent<T> createAndPopulate(EarthGenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
            T data = this.populator.populate(settings, pos, regionSize, width, height);
            return new RegionComponent<>(this.type, data);
        }
    }

    private static class AttachedDependentComponentType<T, V> {
        private final RegionComponentType<T> type;
        private final RegionComponentType<V> dependentType;
        private final DependentRegionPopulator<T, V> populator;

        private AttachedDependentComponentType(RegionComponentType<T> type, RegionComponentType<V> dependentType, DependentRegionPopulator<T, V> populator) {
            this.type = type;
            this.dependentType = dependentType;
            this.populator = populator;
        }

        @SuppressWarnings("unchecked")
        public RegionComponent<T> createAndPopulate(Map<RegionComponentType<?>, RegionComponent<?>> populated, EarthGenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
            V dependentData = (V) populated.get(this.dependentType).getData();
            if (dependentData == null) {
                throw new IllegalStateException("Tried to populate dependent type, but required parent was not present!");
            }
            T data = this.populator.populate(settings, pos, regionSize, width, height, dependentData);
            return new RegionComponent<>(this.type, data);
        }
    }
}
