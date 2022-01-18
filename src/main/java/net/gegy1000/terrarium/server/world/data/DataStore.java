package net.gegy1000.terrarium.server.world.data;

public final class DataStore {
    private final Object[] table = new Object[DataKey.keyCount()];

    public <T> void put(DataKey<T> key, T value) {
        this.table[key.id] = value;
    }

    @SuppressWarnings("unchecked")
    public <T> void putUnchecked(DataKey<T> key, Object value) {
        this.put(key, (T) value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(DataKey<T> key) {
        return (T) this.table[key.id];
    }

    public void remove(DataKey<?> key) {
        this.table[key.id] = null;
    }
}
