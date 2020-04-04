package net.gegy1000.earth.server.shared;

import net.gegy1000.earth.server.util.ProcessTracker;
import net.gegy1000.earth.server.world.data.EarthApiKeys;
import net.gegy1000.earth.server.world.data.index.DataIndex1;
import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class SharedEarthData {
    public static final Key<EarthApiKeys> API_KEYS = Key.create();
    public static final Key<WorldClimateRaster> CLIMATIC_VARIABLES = Key.create();
    public static final Key<DataIndex1> REMOTE_INDEX = Key.create();

    private static SharedEarthData loaded;

    private final Map<Key<?>, Object> data = new HashMap<>();

    SharedEarthData() {
    }

    public static void supply(SharedEarthData data) {
        if (loaded != null) {
            throw new IllegalStateException("Already initialized");
        }
        loaded = data;
    }

    public static SharedEarthData instance() {
        if (loaded == null) {
            loaded = SharedDataInitializers.initialize(new ProcessTracker()).join();
        }
        return loaded;
    }

    public static boolean isInitialized() {
        return loaded != null;
    }

    public <T> void put(Key<T> key, T value) {
        this.data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(Key<T> key) {
        return (T) this.data.get(key);
    }

    public static class Key<T> {
        private Key() {
        }

        public static <T> Key<T> create() {
            return new Key<>();
        }
    }
}
