package net.gegy1000.terrarium.server.world.data;

import com.google.common.collect.ImmutableMap;

import java.util.Optional;
import java.util.Set;

public class ColumnData {
    private final ImmutableMap<DataKey<?>, Optional<?>> store;

    ColumnData(ImmutableMap<DataKey<?>, Optional<?>> store) {
        this.store = store;
    }

    @SuppressWarnings({ "unchecked", "OptionalAssignedToNull" })
    public <T> Optional<T> get(DataKey<T> key) {
        Optional<?> data = this.store.get(key);
        if (data == null) {
            return Optional.empty();
        }
        return (Optional<T>) data;
    }

    public Set<DataKey<?>> keys() {
        return this.store.keySet();
    }
}
