package net.gegy1000.terrarium.server.world.data;

import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public final class DataSample {
    private final DataView view;
    private final DataStore store;

    DataSample(DataView view, DataStore store) {
        this.view = view;
        this.store = store;
    }

    @Nonnull
    public DataView getView() {
        return this.view;
    }

    public <T> boolean contains(DataKey<T> key) {
        return this.store.get(key) != null;
    }

    public <T> Optional<T> get(DataKey<T> key) {
        return Optional.ofNullable(this.store.get(key));
    }

    @Nullable
    public  <T> T getOrNull(DataKey<T> key) {
        return this.store.get(key);
    }

    public <T> T getOrDefault(DataKey<T> key) {
        T value = this.store.get(key);
        if (value != null) {
            return value;
        } else {
            return key.createDefault(this.view);
        }
    }

    public Optional<With> with(DataKey<?>... keys) {
        for (DataKey<?> key : keys) {
            Object value = this.store.get(key);
            if (value == null) return Optional.empty();
        }
        return Optional.of(new With(keys));
    }

    public final class With {
        private final DataKey<?>[] keys;

        private With(DataKey<?>[] keys) {
            this.keys = keys;
        }

        public <T> T get(DataKey<T> key) {
            T value = DataSample.this.getOrNull(key);
            if (value != null && ArrayUtils.contains(this.keys, key)) {
                return value;
            } else {
                throw new IllegalStateException("tried to access missing data with key " + key);
            }
        }
    }
}
