package net.gegy1000.terrarium.server.world.region;

import com.google.common.collect.ImmutableMap;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponent;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;

import javax.annotation.Nullable;
import java.util.Map;

public class RegionData {
    private final ImmutableMap<RegionComponentType<?>, RegionComponent<?>> attachedComponents;

    public RegionData(Map<RegionComponentType<?>, RegionComponent<?>> attachedComponents) {
        this.attachedComponents = ImmutableMap.copyOf(attachedComponents);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Data> T get(RegionComponentType<T> componentType) {
        RegionComponent<?> component = this.attachedComponents.get(componentType);
        if (component != null) {
            return ((RegionComponent<T>) component).getData();
        }
        return null;
    }

    public <T extends Data> T getOrExcept(RegionComponentType<T> componentType) throws IllegalArgumentException {
        T value = this.get(componentType);
        if (value == null) {
            throw new IllegalArgumentException("Component of type " + componentType.getIdentifier() + " not found!");
        }
        return value;
    }
}
