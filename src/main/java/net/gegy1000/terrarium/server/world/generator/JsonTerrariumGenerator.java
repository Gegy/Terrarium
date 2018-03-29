package net.gegy1000.terrarium.server.world.generator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateStateRegistry;
import net.gegy1000.terrarium.server.world.coordinate.SpawnpointDefinition;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyContainer;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.widget.CustomizationCategory;
import net.gegy1000.terrarium.server.world.generator.customization.widget.WidgetParseHandler;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.ParsableInstanceObject;
import net.gegy1000.terrarium.server.world.json.PropertyJsonValueParser;
import net.gegy1000.terrarium.server.world.json.TerrariumJsonUtils;
import net.gegy1000.terrarium.server.world.pipeline.DataPipelineRegistries;
import net.gegy1000.terrarium.server.world.pipeline.RegionDataSystem;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.AttachedComponent;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.ComposerRegistries;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.populator.RegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.source.Geocoder;
import net.gegy1000.terrarium.server.world.pipeline.source.GeocoderRegistry;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonTerrariumGenerator implements TerrariumGenerator {
    private final ResourceLocation identifier;
    private final ImmutableList<CustomizationCategory> categories;
    private final ImmutableMap<String, ParsableInstanceObject<CoordinateState>> coordinateStates;
    private final ImmutableList<AttachedComponent.Parsable<?>> components;
    private final ImmutableList<ParsableInstanceObject<RegionAdapter>> adapters;
    private final String navigationalStateKey;
    private final SpawnpointDefinition spawnpointDefinition;
    private final ParsableInstanceObject<Geocoder> geocoder;
    private final ImmutableList<ParsableInstanceObject<SurfaceComposer>> surfaceComposers;
    private final ImmutableList<ParsableInstanceObject<DecorationComposer>> decorationComposers;
    private final ParsableInstanceObject<BiomeComposer> biomeComposer;
    private final PropertyContainer constants;

    private JsonTerrariumGenerator(
            ResourceLocation identifier,
            List<CustomizationCategory> categories,
            Map<String, ParsableInstanceObject<CoordinateState>> coordinateStates,
            List<AttachedComponent.Parsable<?>> components,
            List<ParsableInstanceObject<RegionAdapter>> adapters,
            String navigationalStateKey,
            SpawnpointDefinition spawnpointDefinition,
            ParsableInstanceObject<Geocoder> geocoder,
            List<ParsableInstanceObject<SurfaceComposer>> surfaceComposers,
            List<ParsableInstanceObject<DecorationComposer>> decorationComposers,
            ParsableInstanceObject<BiomeComposer> biomeComposer,
            PropertyContainer constants
    ) {
        this.identifier = identifier;
        this.categories = ImmutableList.copyOf(categories);
        this.coordinateStates = ImmutableMap.copyOf(coordinateStates);
        this.components = ImmutableList.copyOf(components);
        this.adapters = ImmutableList.copyOf(adapters);
        this.navigationalStateKey = navigationalStateKey;
        this.spawnpointDefinition = spawnpointDefinition;
        this.geocoder = geocoder;
        this.surfaceComposers = ImmutableList.copyOf(surfaceComposers);
        this.decorationComposers = ImmutableList.copyOf(decorationComposers);
        this.biomeComposer = biomeComposer;
        this.constants = constants;
    }

    // TODO: Clean this mess up. Ideally we want some sort of system to handle and log errors.
    public static JsonTerrariumGenerator parseGenerator(JsonObject root) {
        try {
            ResourceLocation identifier = new ResourceLocation(JsonUtils.getString(root, "identifier"));

            JsonObject customizationRoot = TerrariumJsonUtils.parseRemoteObject(root, "customization");

            JsonObject propertiesRoot = TerrariumJsonUtils.parseRemoteObject(customizationRoot, "properties");
            Map<String, PropertyKey<?>> properties = PropertyKey.parseProperties(propertiesRoot);

            JsonObject coordinateSystemRoot = TerrariumJsonUtils.parseRemoteObject(root, "coordinate_system");
            Map<String, ParsableInstanceObject<CoordinateState>> coordinateStateParsers = JsonTerrariumGenerator.parseCoordinateStates(coordinateSystemRoot);

            String navigationalStateKey = JsonUtils.getString(coordinateSystemRoot, "navigational_state");
            if (!coordinateStateParsers.containsKey(navigationalStateKey)) {
                throw new JsonSyntaxException("Navigational state " + navigationalStateKey + " does not exist");
            }

            JsonObject spawnpointRoot = TerrariumJsonUtils.parseRemoteObject(coordinateSystemRoot, "spawnpoint");
            SpawnpointDefinition spawnpointDefinition = JsonTerrariumGenerator.parseSpawnpoint(spawnpointRoot, properties);

            JsonObject geocoderRoot = TerrariumJsonUtils.parseRemoteObject(coordinateSystemRoot, "geocoder");
            ResourceLocation geocoderKey = new ResourceLocation(JsonUtils.getString(geocoderRoot, "type"));
            InstanceObjectParser<Geocoder> geocoderParser = GeocoderRegistry.getGeocoder(geocoderKey);
            if (geocoderParser == null) {
                throw new JsonSyntaxException("Geocoder " + geocoderKey + " does not exist");
            }

            ParsableInstanceObject<Geocoder> geocoder = new ParsableInstanceObject<>(geocoderParser, geocoderRoot);

            JsonObject dataSystemRoot = TerrariumJsonUtils.parseRemoteObject(root, "data_system");

            JsonArray componentsArray = TerrariumJsonUtils.parseRemoteArray(dataSystemRoot, "components");
            List<AttachedComponent.Parsable<?>> componentParsers = JsonTerrariumGenerator.parseComponents(componentsArray);

            JsonArray adaptersArray = TerrariumJsonUtils.parseRemoteArray(dataSystemRoot, "adapters");
            List<ParsableInstanceObject<RegionAdapter>> adapterParsers = JsonTerrariumGenerator.parseAdapters(adaptersArray);

            JsonObject chunkComposerRoot = TerrariumJsonUtils.parseRemoteObject(root, "chunk_composer");

            JsonArray surfaceComposersArray = TerrariumJsonUtils.parseRemoteArray(chunkComposerRoot, "surface_composers");
            List<ParsableInstanceObject<SurfaceComposer>> surfaceComposers = JsonTerrariumGenerator.parseSurfaceComposers(surfaceComposersArray);

            JsonArray decorationComposersArray = TerrariumJsonUtils.parseRemoteArray(chunkComposerRoot, "decoration_composers");
            List<ParsableInstanceObject<DecorationComposer>> decorationComposers = JsonTerrariumGenerator.parseDecorationComposers(decorationComposersArray);

            JsonObject biomeComposerRoot = TerrariumJsonUtils.parseRemoteObject(chunkComposerRoot, "biome_composer");
            ParsableInstanceObject<BiomeComposer> biomeComposer = JsonTerrariumGenerator.parseBiomeComposer(biomeComposerRoot);

            JsonObject constantsRoot = TerrariumJsonUtils.parseRemoteObject(root, "constants");
            PropertyContainer constants = PropertyContainer.deserialize(constantsRoot);

            WidgetParseHandler widgetParseHandler = new WidgetParseHandler(properties, new PropertyJsonValueParser.Container(constants));

            JsonObject categoryRoot = TerrariumJsonUtils.parseRemoteObject(customizationRoot, "categories");
            List<CustomizationCategory> categories = CustomizationCategory.parseCategories(widgetParseHandler, categoryRoot);

            return new JsonTerrariumGenerator(
                    identifier, categories,
                    coordinateStateParsers, componentParsers, adapterParsers,
                    navigationalStateKey, spawnpointDefinition, geocoder,
                    surfaceComposers, decorationComposers, biomeComposer,
                    constants);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new JsonSyntaxException(e);
        }
    }

    private static Map<String, ParsableInstanceObject<CoordinateState>> parseCoordinateStates(JsonObject root) {
        JsonObject statesRoot = TerrariumJsonUtils.parseRemoteObject(root, "states");
        Map<String, ParsableInstanceObject<CoordinateState>> coordinateStates = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : statesRoot.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                JsonObject stateRoot = entry.getValue().getAsJsonObject();
                ResourceLocation stateType = new ResourceLocation(JsonUtils.getString(stateRoot, "type"));
                InstanceObjectParser<CoordinateState> stateParser = CoordinateStateRegistry.get(stateType);
                if (stateParser != null) {
                    coordinateStates.put(entry.getKey(), new ParsableInstanceObject<>(stateParser, stateRoot));
                } else {
                    throw new JsonSyntaxException("Found invalid coordinate state type " + stateType);
                }
            } else {
                Terrarium.LOGGER.warn("Ignored invalid coordinate state element {}", entry.getKey());
            }
        }
        return coordinateStates;
    }

    private static List<AttachedComponent.Parsable<?>> parseComponents(JsonArray componentsArray) {
        List<AttachedComponent.Parsable<?>> components = new ArrayList<>();

        for (JsonElement element : componentsArray) {
            if (element.isJsonObject()) {
                JsonObject componentRoot = element.getAsJsonObject();
                ResourceLocation typeIdentifier = new ResourceLocation(JsonUtils.getString(componentRoot, "type"));
                RegionComponentType<?> componentType = DataPipelineRegistries.getComponentType(typeIdentifier);

                if (componentType != null) {
                    components.add(JsonTerrariumGenerator.parseComponent(componentRoot, componentType));
                } else {
                    throw new JsonSyntaxException("Found invalid component type " + typeIdentifier);
                }
            } else {
                Terrarium.LOGGER.warn("Ignored invalid component element {}", element);
            }
        }

        return components;
    }

    @SuppressWarnings("unchecked")
    private static <T> AttachedComponent.Parsable<T> parseComponent(JsonObject componentRoot, RegionComponentType<T> componentType) {
        JsonObject populatorRoot = TerrariumJsonUtils.parseRemoteObject(componentRoot, "populator");
        ResourceLocation populatorType = new ResourceLocation(JsonUtils.getString(populatorRoot, "type"));
        InstanceObjectParser<? extends RegionPopulator<?>> populatorParser = DataPipelineRegistries.getPopulator(populatorType);

        if (populatorParser == null) {
            throw new JsonSyntaxException("Found invalid populator type " + populatorType);
        }

        InstanceObjectParser<RegionPopulator<T>> castedParser = (InstanceObjectParser<RegionPopulator<T>>) populatorParser;
        ParsableInstanceObject<RegionPopulator<T>> parsablePopulator = new ParsableInstanceObject<>(castedParser, populatorRoot);
        return new AttachedComponent.Parsable<>(componentType, parsablePopulator);
    }

    private static List<ParsableInstanceObject<RegionAdapter>> parseAdapters(JsonArray adaptersArray) {
        List<ParsableInstanceObject<RegionAdapter>> adapters = new ArrayList<>();

        for (JsonElement element : adaptersArray) {
            if (element.isJsonObject()) {
                JsonObject adapterRoot = element.getAsJsonObject();
                ResourceLocation typeIdentifier = new ResourceLocation(JsonUtils.getString(adapterRoot, "type"));
                InstanceObjectParser<RegionAdapter> adapterParser = DataPipelineRegistries.getAdapter(typeIdentifier);

                if (adapterParser != null) {
                    adapters.add(new ParsableInstanceObject<>(adapterParser, adapterRoot));
                } else {
                    throw new JsonSyntaxException("Found invalid adapter type " + typeIdentifier);
                }
            } else {
                Terrarium.LOGGER.warn("Ignored invalid adapter element {}", element);
            }
        }

        return adapters;
    }

    @SuppressWarnings("unchecked")
    private static SpawnpointDefinition parseSpawnpoint(JsonObject spawnpointRoot, Map<String, PropertyKey<?>> properties) {
        String propertyXKey = JsonUtils.getString(spawnpointRoot, "x");
        PropertyKey<?> propertyX = properties.get(propertyXKey);
        if (propertyX == null || propertyX.getType() != Number.class) {
            throw new JsonSyntaxException("Invalid property " + propertyXKey + " for spawnpoint x");
        }

        String propertyZKey = JsonUtils.getString(spawnpointRoot, "z");
        PropertyKey<?> propertyZ = properties.get(propertyZKey);
        if (propertyZ == null || propertyZ.getType() != Number.class) {
            throw new JsonSyntaxException("Invalid property " + propertyZKey + " for spawnpoint z");
        }

        return new SpawnpointDefinition((PropertyKey<Number>) propertyX, (PropertyKey<Number>) propertyZ);
    }

    private static List<ParsableInstanceObject<SurfaceComposer>> parseSurfaceComposers(JsonArray composerArray) {
        List<ParsableInstanceObject<SurfaceComposer>> composers = new ArrayList<>();
        for (JsonElement element : composerArray) {
            if (element.isJsonObject()) {
                JsonObject composerRoot = element.getAsJsonObject();
                ResourceLocation typeIdentifier = new ResourceLocation(JsonUtils.getString(composerRoot, "type"));
                InstanceObjectParser<SurfaceComposer> composerParser = ComposerRegistries.getSurfaceComposer(typeIdentifier);

                if (composerParser != null) {
                    composers.add(new ParsableInstanceObject<>(composerParser, composerRoot));
                } else {
                    throw new JsonSyntaxException("Found invalid surface composer type " + typeIdentifier);
                }
            } else {
                Terrarium.LOGGER.warn("Ignored invalid surface composers element {}", element);
            }
        }

        return composers;
    }

    private static List<ParsableInstanceObject<DecorationComposer>> parseDecorationComposers(JsonArray composerArray) {
        List<ParsableInstanceObject<DecorationComposer>> composers = new ArrayList<>();
        for (JsonElement element : composerArray) {
            if (element.isJsonObject()) {
                JsonObject composerRoot = element.getAsJsonObject();
                ResourceLocation typeIdentifier = new ResourceLocation(JsonUtils.getString(composerRoot, "type"));
                InstanceObjectParser<DecorationComposer> composerParser = ComposerRegistries.getDecorationComposer(typeIdentifier);

                if (composerParser != null) {
                    composers.add(new ParsableInstanceObject<>(composerParser, composerRoot));
                } else {
                    throw new JsonSyntaxException("Found invalid decoration composer type " + typeIdentifier);
                }
            } else {
                Terrarium.LOGGER.warn("Ignored invalid decoration composers element {}", element);
            }
        }

        return composers;
    }

    private static ParsableInstanceObject<BiomeComposer> parseBiomeComposer(JsonObject composerRoot) {
        ResourceLocation typeIdentifier = new ResourceLocation(JsonUtils.getString(composerRoot, "type"));
        InstanceObjectParser<BiomeComposer> composerParser = ComposerRegistries.getBiomeComposer(typeIdentifier);

        if (composerParser != null) {
            return new ParsableInstanceObject<>(composerParser, composerRoot);
        } else {
            throw new JsonSyntaxException("Found invalid biome composer type " + typeIdentifier);
        }
    }

    public ResourceLocation getIdentifier() {
        return this.identifier;
    }

    @Override
    public ImmutableList<CustomizationCategory> getCategories() {
        return this.categories;
    }

    @Override
    public RegionDataSystem buildDataSystem(TerrariumWorldData worldData, World world) {
        RegionDataSystem.Builder builder = RegionDataSystem.builder();

        InstanceJsonValueParser valueParser = new InstanceJsonValueParser(worldData, world, worldData.getSettings().getProperties(), this.constants);

        for (AttachedComponent.Parsable<?> attachedComponent : this.components) {
            builder = builder.withParsableComponent(worldData, world, valueParser, attachedComponent);
        }

        for (ParsableInstanceObject<RegionAdapter> adapter : this.adapters) {
            builder = builder.withAdapter(adapter.parse(worldData, world, valueParser));
        }

        return builder.build();
    }

    @Override
    public Map<String, CoordinateState> buildCoordinateStates(TerrariumWorldData worldData, World world) {
        Map<String, CoordinateState> entries = new HashMap<>();

        InstanceJsonValueParser valueParser = new InstanceJsonValueParser(worldData, world, worldData.getSettings().getProperties(), this.constants);

        for (Map.Entry<String, ParsableInstanceObject<CoordinateState>> entry : this.coordinateStates.entrySet()) {
            entries.put(entry.getKey(), entry.getValue().parse(worldData, world, valueParser));
        }

        return entries;
    }

    @Override
    public String getNavigationalStateKey() {
        return this.navigationalStateKey;
    }

    @Override
    public SpawnpointDefinition getSpawnpointDefinition() {
        return this.spawnpointDefinition;
    }

    @Override
    public Geocoder createGeocoder(TerrariumWorldData worldData, World world) {
        return this.geocoder.parse(worldData, world, this.createParser(worldData, world));
    }

    @Override
    public List<SurfaceComposer> createSurfaceComposers(TerrariumWorldData worldData, World world) {
        InstanceJsonValueParser valueParser = this.createParser(worldData, world);
        return this.surfaceComposers.stream().map(parser -> parser.parse(worldData, world, valueParser)).collect(Collectors.toList());
    }

    @Override
    public List<DecorationComposer> createDecorationComposers(TerrariumWorldData worldData, World world) {
        InstanceJsonValueParser valueParser = this.createParser(worldData, world);
        return this.decorationComposers.stream().map(parser -> parser.parse(worldData, world, valueParser)).collect(Collectors.toList());
    }

    @Override
    public BiomeComposer createBiomeComposer(TerrariumWorldData worldData, World world) {
        if (this.biomeComposer != null) {
            InstanceJsonValueParser valueParser = this.createParser(worldData, world);
            return this.biomeComposer.parse(worldData, world, valueParser);
        }
        return null;
    }

    private InstanceJsonValueParser createParser(TerrariumWorldData worldData, World world) {
        return new InstanceJsonValueParser(worldData, world, worldData.getSettings().getProperties(), this.constants);
    }
}
