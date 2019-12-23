package net.gegy1000.terrarium.server.util;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class WeightedPool<T> implements Iterable<WeightedPool.Entry<T>> {
    private final List<Entry<T>> entries;
    private float totalWeight;

    private WeightedPool(List<Entry<T>> entries) {
        this.entries = entries;
        for (Entry<T> entry : entries) {
            this.totalWeight += entry.weight;
        }
    }

    public WeightedPool() {
        this.entries = new ArrayList<>();
    }

    public WeightedPool<T> add(T value, float weight) {
        if (weight <= 0) throw new IllegalArgumentException("weight must be positive");
        this.entries.add(new Entry<>(value, weight));
        this.totalWeight += weight;
        return this;
    }

    public WeightedPool<T> remove(T value) {
        this.entries.removeIf(e -> e.value.equals(value));
        return this;
    }

    public WeightedPool<T> addAll(Iterable<WeightedPool.Entry<T>> entries) {
        for (WeightedPool.Entry<T> entry : entries) {
            this.entries.add(entry);
            this.totalWeight += entry.weight;
        }
        return this;
    }

    @Nullable
    public T sampleOrNull(Random random) {
        float sample = random.nextFloat() * this.totalWeight;

        float w = 0;
        for (Entry<T> entry : this.entries) {
            w += entry.weight;
            if (w > sample) return entry.value;
        }

        return null;
    }

    @Nonnull
    public T sampleOrExcept(Random random) {
        return Preconditions.checkNotNull(this.sampleOrNull(random), "empty pool");
    }

    public Entry<T> get(int index) {
        return this.entries.get(index);
    }

    public int size() {
        return this.entries.size();
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("WeightedPool{entries=%d, totalWeight=%s}", this.entries.size(), this.totalWeight);
    }

    @Override
    public Iterator<Entry<T>> iterator() {
        return this.entries.iterator();
    }

    public static class Entry<T> {
        private final T value;
        private final float weight;

        Entry(T value, float weight) {
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
