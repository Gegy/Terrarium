package net.gegy1000.earth.server.world;

import com.google.gson.JsonElement;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.PropertySchema;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyPair;

import java.util.Map;
import java.util.function.Consumer;

import static net.gegy1000.earth.server.world.EarthProperties.*;

public final class EarthPropertySchema implements PropertySchema {
    public static final EarthPropertySchema INSTANCE = new EarthPropertySchema();

    private EarthPropertySchema() {
    }

    @Override
    public boolean parse(Map<String, JsonElement> entries, Consumer<PropertyPair<?>> consumer) {
        int version = this.detectVersion(entries);

        if (version == 0) {
            return this.parseLegacy(entries, consumer);
        } else {
            return this.parseLatest(entries, consumer);
        }
    }

    private int detectVersion(Map<String, JsonElement> entries) {
        if (this.matchesLegacy(entries)) {
            return 0;
        }

        return 1;
    }

    private boolean matchesLegacy(Map<String, JsonElement> entries) {
        for (PropertyKey<?> legacy : Legacy.PROPERTIES) {
            if (!entries.containsKey(legacy.getIdentifier())) {
                return false;
            }
        }
        return true;
    }

    private boolean parseLatest(Map<String, JsonElement> entries, Consumer<PropertyPair<?>> consumer) {
        return SCHEMA.parse(entries, consumer);
    }

    private boolean parseLegacy(Map<String, JsonElement> entries, Consumer<PropertyPair<?>> consumer) {
        GenerationSettings.Builder legacyBuilder = GenerationSettings.builder();

        if (Legacy.SCHEMA.parse(entries, legacyBuilder::put)) {
            GenerationSettings legacy = legacyBuilder.build();

            double spawnLatitude = legacy.getDouble(Legacy.SPAWN_LATITUDE);
            double spawnLongitude = legacy.getDouble(Legacy.SPAWN_LONGITUDE);

            consumer.accept(PropertyPair.of(SPAWN_LATITUDE, spawnLatitude));
            consumer.accept(PropertyPair.of(SPAWN_LONGITUDE, spawnLongitude));

            double worldScale = 1.0 / legacy.getDouble(Legacy.WORLD_SCALE);
            double heightScale = legacy.getDouble(Legacy.HEIGHT_SCALE);
            int heightOrigin = legacy.getInteger(Legacy.HEIGHT_ORIGIN);

            consumer.accept(PropertyPair.of(WORLD_SCALE, worldScale));
            consumer.accept(PropertyPair.of(TERRESTRIAL_HEIGHT_SCALE, heightScale));
            consumer.accept(PropertyPair.of(OCEANIC_HEIGHT_SCALE, heightScale));
            consumer.accept(PropertyPair.of(HEIGHT_OFFSET, heightOrigin));

            boolean decoration = legacy.getBoolean(Legacy.ENABLE_DECORATION);
            consumer.accept(PropertyPair.of(ADD_TREES, decoration));
            consumer.accept(PropertyPair.of(ADD_GRASS, decoration));

            boolean defaultDecoration = legacy.getBoolean(Legacy.ENABLE_DEFAULT_DECORATION);
            consumer.accept(PropertyPair.of(ADD_FLOWERS, defaultDecoration));
            consumer.accept(PropertyPair.of(ADD_CACTI, defaultDecoration));
            consumer.accept(PropertyPair.of(ADD_SUGAR_CANE, defaultDecoration));
            consumer.accept(PropertyPair.of(ADD_GOURDS, defaultDecoration));

            boolean defaultFeatures = legacy.getBoolean(Legacy.ENABLE_DEFAULT_FEATURES);
            consumer.accept(PropertyPair.of(ADD_STRONGHOLDS, defaultFeatures));
            consumer.accept(PropertyPair.of(ADD_VILLAGES, defaultFeatures));
            consumer.accept(PropertyPair.of(ADD_MINESHAFTS, defaultFeatures));
            consumer.accept(PropertyPair.of(ADD_TEMPLES, defaultFeatures));
            consumer.accept(PropertyPair.of(ADD_OCEAN_MONUMENTS, defaultFeatures));
            consumer.accept(PropertyPair.of(ADD_WOODLAND_MANSIONS, defaultFeatures));

            boolean caveGeneration = legacy.getBoolean(Legacy.ENABLE_CAVE_GENERATION);
            consumer.accept(PropertyPair.of(CAVE_GENERATION, caveGeneration));
            consumer.accept(PropertyPair.of(RAVINE_GENERATION, caveGeneration));

            boolean resourceGeneration = legacy.getBoolean(Legacy.ENABLE_RESOURCE_GENERATION);
            consumer.accept(PropertyPair.of(ORE_GENERATION, resourceGeneration));

            boolean modGeneration = legacy.getBoolean(Legacy.ENABLE_MOD_GENERATION);
            consumer.accept(PropertyPair.of(COMPATIBILITY_MODE, modGeneration));

            boolean lavaGeneration = legacy.getBoolean(Legacy.ENABLE_LAVA_GENERATION);
            consumer.accept(PropertyPair.of(ADD_LAVA_POOLS, lavaGeneration));

            consumer.accept(PropertyPair.of(BOP_INTEGRATION, false));

            return true;
        } else {
            TerrariumEarth.LOGGER.warn("Failed to parse legacy schema!");
            return false;
        }
    }
}
