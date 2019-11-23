package net.gegy1000.terrarium.server.world.pipeline.data;

import com.google.common.collect.ImmutableMap;

import java.util.Optional;

public class ColumnData {
    private final ImmutableMap<DataKey<?>, Optional<?>> store;

    ColumnData(ImmutableMap<DataKey<?>, Optional<?>> store) {
        this.store = store;
    }

    @SuppressWarnings({ "unchecked", "OptionalAssignedToNull" })
    public <T> Optional<T> get(DataKey<T> key) throws IllegalArgumentException {
        Optional<?> data = this.store.get(key);
        if (data == null) {
            throw new IllegalArgumentException("Data with key " + key.getIdentifier() + " not found!");
        }
        return (Optional<T>) data;
    }
}
