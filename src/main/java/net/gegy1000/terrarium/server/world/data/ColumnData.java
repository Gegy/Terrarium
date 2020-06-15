package net.gegy1000.terrarium.server.world.data;

import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

public final class ColumnData {
    private final DataView view;
    private final DataStore<Optional<?>> store;

    ColumnData(DataView view, DataStore<Optional<?>> store) {
        this.view = view;
        this.store = store;
    }

    @Nonnull
    public DataView getView() {
        return this.view;
    }

    @SuppressWarnings({ "unchecked", "OptionalAssignedToNull" })
    public <T> Optional<T> get(DataKey<T> key) {
        Optional<?> data = this.store.get(key);
        if (data == null) {
            return Optional.empty();
        }
        return (Optional<T>) data;
    }

    public <T> T getOrDefault(DataKey<T> key) {
        return this.get(key).orElseGet(() -> key.createDefault(this.view));
    }

    public Optional<With> with(DataKey<?>... keys) {
        for (DataKey<?> key : keys) {
            Optional<?> value = this.store.get(key);
            if (value == null || !value.isPresent()) return Optional.empty();
        }
        return Optional.of(new With(Sets.newHashSet(keys)));
    }

    public final class With {
        private final Set<DataKey<?>> keys;

        private With(Set<DataKey<?>> keys) {
            this.keys = keys;
        }

        public <T> T get(DataKey<T> key) {
            if (!this.keys.contains(key)) throw new IllegalArgumentException();
            return ColumnData.this.get(key).orElseThrow(IllegalStateException::new);
        }
    }
}
