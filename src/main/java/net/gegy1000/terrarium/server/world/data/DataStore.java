package net.gegy1000.terrarium.server.world.data;

public final class DataStore<T> {
    private final Object[] table = new Object[DataKey.keyCount()];

    public void put(DataKey<?> key, T value) {
        this.table[key.id] = value;
    }

    @SuppressWarnings("unchecked")
    public T get(DataKey<?> key) {
        return (T) this.table[key.id];
    }

    public boolean containsKey(DataKey<?> key) {
        return this.table[key.id] != null;
    }

    public void remove(DataKey<?> key) {
        this.table[key.id] = null;
    }
}
