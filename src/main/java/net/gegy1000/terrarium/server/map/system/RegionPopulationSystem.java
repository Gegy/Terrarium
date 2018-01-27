package net.gegy1000.terrarium.server.map.system;

import com.google.common.collect.ImmutableList;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.RegionData;
import net.gegy1000.terrarium.server.map.RegionTilePos;
import net.gegy1000.terrarium.server.map.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.map.system.component.RegionComponent;
import net.gegy1000.terrarium.server.map.system.component.RegionComponentType;
import net.gegy1000.terrarium.server.map.system.populator.RegionPopulator;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionPopulationSystem {
    private final EarthGenerationSettings settings;

    private final ImmutableList<AttachedComponentType<?>> attachedComponents;
    private final ImmutableList<RegionAdapter> adapters;

    private RegionPopulationSystem(EarthGenerationSettings settings, ImmutableList<AttachedComponentType<?>> attachedComponents, ImmutableList<RegionAdapter> adapters) {
        this.settings = settings;
        this.attachedComponents = attachedComponents;
        this.adapters = adapters;
    }

    public static Builder builder(EarthGenerationSettings settings) {
        return new Builder(settings);
    }

    public RegionData populateData(RegionTilePos pos, Coordinate regionSize, int width, int height) {
        Map<RegionComponentType<?>, RegionComponent<?>> populatedComponents = new HashMap<>();
        for (AttachedComponentType<?> attachedComponent : this.attachedComponents) {
            RegionComponent<?> component = attachedComponent.createAndPopulate(this.settings, pos, regionSize, width, height);
            populatedComponents.put(attachedComponent.type, component);
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
        private final List<AttachedComponentType<?>> attachedComponents = new ArrayList<>();
        private final List<RegionAdapter> adapters = new ArrayList<>();

        private Builder(EarthGenerationSettings settings) {
            this.settings = settings;
        }

        public <T> Builder withComponent(RegionComponentType<T> type, RegionPopulator<T> populator) {
            this.attachedComponents.add(new AttachedComponentType<>(type, populator));
            return this;
        }

        public Builder withAdapter(RegionAdapter adapter) {
            this.adapters.add(adapter);
            return this;
        }

        public RegionPopulationSystem build() {
            return new RegionPopulationSystem(this.settings, ImmutableList.copyOf(this.attachedComponents), ImmutableList.copyOf(this.adapters));
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
}
