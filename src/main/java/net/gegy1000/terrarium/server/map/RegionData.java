package net.gegy1000.terrarium.server.map;

import com.google.common.collect.ImmutableMap;
import net.gegy1000.terrarium.server.map.system.component.RegionComponent;
import net.gegy1000.terrarium.server.map.system.component.RegionComponentType;

import javax.annotation.Nullable;
import java.util.Map;

public class RegionData {
    private final ImmutableMap<RegionComponentType<?>, RegionComponent<?>> attachedComponents;

    public RegionData(Map<RegionComponentType<?>, RegionComponent<?>> attachedComponents) {
        this.attachedComponents = ImmutableMap.copyOf(attachedComponents);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(RegionComponentType<T> componentType) {
        RegionComponent<?> component = this.attachedComponents.get(componentType);
        if (component != null) {
            return ((RegionComponent<T>) component).getData();
        }
        return null;
    }
}
