package net.gegy1000.earth.server.world.biome;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.gegy1000.earth.TerrariumEarth;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BiomeClassification {
    private static final Set<Biome> EXCLUDED_BIOMES = Stream.of(
            BiomeDictionary.Type.END,
            BiomeDictionary.Type.NETHER,
            BiomeDictionary.Type.MAGICAL,
            BiomeDictionary.Type.VOID,
            BiomeDictionary.Type.SPOOKY,
            BiomeDictionary.Type.HILLS,
            BiomeDictionary.Type.MOUNTAIN
    )
            .flatMap(t -> BiomeDictionary.getBiomes(t).stream())
            .collect(Collectors.toSet());

    private static final int POOL_SIZE = 16;
    private static final LinkedList<BiomeClassification> POOL = new LinkedList<>();

    private final Object2IntOpenHashMap<Biome> frequencies = new Object2IntOpenHashMap<>();
    private final Set<Biome> exclude = new HashSet<>(EXCLUDED_BIOMES);

    private Biome matched = Biomes.PLAINS;
    private int matchedFrequency;

    public static BiomeClassification take() {
        if (POOL.isEmpty()) {
            return new BiomeClassification();
        }
        return POOL.removeLast();
    }

    private static void returnToPool(BiomeClassification classification) {
        POOL.addLast(classification);

        if (POOL.size() > POOL_SIZE * 2) {
            TerrariumEarth.LOGGER.warn("Biome classification pool has grown excessively!");
            while (POOL.size() > POOL_SIZE) {
                POOL.removeLast();
            }
        }
    }

    public void include(BiomeDictionary.Type type) {
        for (Biome biome : BiomeDictionary.getBiomes(type)) {
            if (this.exclude.contains(biome)) {
                continue;
            }
            int frequency = this.frequencies.addTo(biome, 1) + 1;
            if (frequency > this.matchedFrequency) {
                this.matched = biome;
                this.matchedFrequency = frequency;
            }
        }
    }

    public void exclude(BiomeDictionary.Type type) {
        for (Biome biome : BiomeDictionary.getBiomes(type)) {
            int frequency = this.frequencies.removeInt(biome);
            if (frequency >= this.matchedFrequency) {
                this.recomputeMatched();
            }

            this.exclude.add(biome);
        }
    }

    private void recomputeMatched() {
        this.matchedFrequency = 0;
        this.matched = Biomes.PLAINS;

        for (Object2IntMap.Entry<Biome> entry : this.frequencies.object2IntEntrySet()) {
            int frequency = entry.getIntValue();
            if (frequency > this.matchedFrequency) {
                this.matched = Biomes.PLAINS;
                this.matchedFrequency = frequency;
            }
        }
    }

    public void release() {
        this.frequencies.clear();

        this.exclude.clear();
        this.exclude.addAll(EXCLUDED_BIOMES);

        this.matched = Biomes.PLAINS;
        this.matchedFrequency = 0;

        returnToPool(this);
    }

    public Biome match() {
        return this.matched;
    }
}
