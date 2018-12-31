package net.gegy1000.terrarium.server.world.customization;

import net.gegy1000.terrarium.server.util.JsonDiscoverer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum TerrariumPresetRegistry implements ResourceReloadListener {
    INSTANCE;

    private static final JsonDiscoverer<TerrariumPreset> DISCOVERER = new JsonDiscoverer<>(TerrariumPreset::parse);
    private final Map<Identifier, TerrariumPreset> presets = new HashMap<>();

    @Override
    public void onResourceReload(ResourceManager manager) {
        this.presets.clear();
        for (JsonDiscoverer.Result<TerrariumPreset> result : DISCOVERER.discoverFiles(manager, "terrarium/presets")) {
            this.presets.put(result.getKey(), result.getParsed());
        }
    }

    public TerrariumPreset get(Identifier identifier) {
        return this.presets.get(identifier);
    }

    public Collection<TerrariumPreset> getPresets() {
        return this.presets.values();
    }

    public Map<Identifier, TerrariumPreset> getRegistry() {
        return Collections.unmodifiableMap(this.presets);
    }
}
