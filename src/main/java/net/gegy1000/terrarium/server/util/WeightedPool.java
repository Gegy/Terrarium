package net.gegy1000.terrarium.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

public final class WeightedPool<T> implements Iterable<WeightedPool.Entry<T>> {
    private final Collection<Entry<T>> entries;
    private final float totalWeight;

    private WeightedPool(Collection<Entry<T>> entries, float totalWeight) {
        this.entries = entries;
        this.totalWeight = totalWeight;
    }

    public T sample(Random random) {
        if (this.entries.isEmpty()) throw new IllegalStateException("Cannot sample empty pool");

        float position = random.nextFloat() * this.totalWeight;

        int w = 0;
        for (Entry<T> entry : this.entries) {
            w += entry.weight;
            if (w > position) {
                return entry.value;
            }
        }

        throw new IllegalStateException();
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    @Override
    public Iterator<Entry<T>> iterator() {
        return this.entries.iterator();
    }

    public static class Builder<T> {
        private final Collection<Entry<T>> entries = new ArrayList<>();
        private float totalWeight;

        private Builder() {
        }

        public Builder<T> with(T value, float weight) {
            if (weight <= 0) {
                throw new IllegalArgumentException("Weight must be >0");
            }
            this.entries.add(new Entry<>(value, weight));
            this.totalWeight += weight;
            return this;
        }

        public WeightedPool<T> build() {
            return new WeightedPool<>(this.entries, this.totalWeight);
        }
    }

    public static class Entry<T> {
        private final T value;
        private final float weight;

        private Entry(T value, float weight) {
            this.value = value;
            this.weight = weight;
        }

        public T getValue() {
            return this.value;
        }

        public float getWeight() {
            return this.weight;
        }
    }
}
