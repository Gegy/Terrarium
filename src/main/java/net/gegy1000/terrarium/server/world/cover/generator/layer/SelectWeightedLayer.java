package net.gegy1000.terrarium.server.world.cover.generator.layer;

import net.minecraft.class_3630;
import net.minecraft.world.biome.layer.InitLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectWeightedLayer implements InitLayer {
    private final Entry[] entries;
    private final int totalWeight;

    private SelectWeightedLayer(Entry[] entries) {
        this.entries = entries;
        this.totalWeight = Arrays.stream(entries).mapToInt(Entry::getWeight).sum();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int sample(class_3630 context, int x, int z) {
        int weight = context.nextInt(this.totalWeight);
        int currentWeight = 0;
        for (Entry entry : this.entries) {
            currentWeight += entry.weight;
            if (currentWeight >= weight) {
                return entry.value;
            }
        }
        return 0;
    }

    private static class Entry {
        private final int value;
        private final int weight;

        private Entry(int value, int weight) {
            this.value = value;
            this.weight = weight;
        }

        int getValue() {
            return this.value;
        }

        int getWeight() {
            return this.weight;
        }
    }

    public static class Builder {
        private final List<Entry> entries = new ArrayList<>();

        public Builder withEntry(int value, int weight) {
            this.entries.add(new Entry(value, weight));
            return this;
        }

        public SelectWeightedLayer build() {
            if (this.entries.isEmpty()) {
                throw new IllegalStateException("No weight entries specified");
            }
            return new SelectWeightedLayer(this.entries.toArray(new Entry[0]));
        }
    }
}
