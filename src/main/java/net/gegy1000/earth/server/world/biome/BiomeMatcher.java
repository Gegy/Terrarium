package net.gegy1000.earth.server.world.biome;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BiomeMatcher {
    // TODO: This is very likely incredibly inefficient
    private static final Map<Key, Biome> CACHE = new HashMap<>();

    private static Set<Biome> EXCLUDED_BIOMES = Stream.of(
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

    public static Biome match(BiomeClassification classification) {
        Key key = key(classification);
        Biome cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        Biome result = computeMatch(classification);
        CACHE.put(key.cloned(), result);

        return result;
    }

    private static Biome computeMatch(BiomeClassification classification) {
        Object2IntMap<Biome> biomes = computeFrequencies(classification);

        Biome result = Biomes.PLAINS;
        int resultFrequency = 0;

        for (Object2IntMap.Entry<Biome> entry : biomes.object2IntEntrySet()) {
            int frequency = entry.getIntValue();
            if (frequency > resultFrequency) {
                resultFrequency = frequency;
                result = entry.getKey();
            }
        }

        return result;
    }

    private static Object2IntMap<Biome> computeFrequencies(BiomeClassification classification) {
        Object2IntOpenHashMap<Biome> biomes = new Object2IntOpenHashMap<>();

        for (BiomeDictionary.Type type : classification.getInclude()) {
            for (Biome biome : BiomeDictionary.getBiomes(type)) {
                if (EXCLUDED_BIOMES.contains(biome)) continue;
                biomes.addTo(biome, 1);
            }
        }

        for (BiomeDictionary.Type type : classification.getExclude()) {
            BiomeDictionary.getBiomes(type).forEach(biomes::removeInt);
        }

        return biomes;
    }

    private static Key key(BiomeClassification classification) {
        return new Key(classification.getInclude(), classification.getExclude());
    }

    private static class Key {
        private final List<BiomeDictionary.Type> include;
        private final List<BiomeDictionary.Type> exclude;

        private Key(List<BiomeDictionary.Type> include, List<BiomeDictionary.Type> exclude) {
            this.include = include;
            this.exclude = exclude;
        }

        @Override
        public int hashCode() {
            return this.include.hashCode() + this.exclude.hashCode() * 31;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj.getClass() == Key.class) {
                Key key = (Key) obj;
                return this.include.equals(key.include) && this.exclude.equals(key.exclude);
            }
            return false;
        }

        public Key cloned() {
            return new Key(Lists.newArrayList(this.include), Lists.newArrayList(this.exclude));
        }
    }
}
