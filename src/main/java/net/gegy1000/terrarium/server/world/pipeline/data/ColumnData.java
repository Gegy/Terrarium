package net.gegy1000.terrarium.server.world.pipeline.data;

import com.google.common.collect.ImmutableMap;

import java.util.Optional;

public class ColumnData {
    private final ImmutableMap<DataKey<?>, Optional<? extends Data>> store;

    ColumnData(ImmutableMap<DataKey<?>, Optional<? extends Data>> store) {
        this.store = store;
    }

    @SuppressWarnings({ "unchecked", "OptionalAssignedToNull" })
    public <T extends Data> Optional<T> get(DataKey<T> key) throws IllegalArgumentException {
        Optional<? extends Data> data = this.store.get(key);
        if (data == null) {
            throw new IllegalArgumentException("Data with key " + key.getIdentifier() + " not found!");
        }
        return (Optional<T>) data;
    }
}
