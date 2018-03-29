package net.gegy1000.terrarium.server.world.generator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateStateRegistry;
import net.gegy1000.terrarium.server.world.coordinate.ScaledCoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.SpawnpointDefinition;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyContainer;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.widget.CustomizationCategory;
import net.gegy1000.terrarium.server.world.generator.customization.widget.WidgetParseHandler;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.json.ParsableInstanceObject;
import net.gegy1000.terrarium.server.world.json.ParseStateHandler;
import net.gegy1000.terrarium.server.world.json.ParseUtils;
import net.gegy1000.terrarium.server.world.json.PropertyJsonValueParser;
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
    public static JsonTerrariumGenerator parseGenerator(JsonObject root) throws InvalidJsonException {
        ResourceLocation identifier = new ResourceLocation(JsonUtils.getString(root, "identifier"));

        try {
            ParseStateHandler.begin();

            ParseStateHandler.pushContext("parsing generator");

            PropertyContainer constants = ParseUtils.parseObject(root, "constants", PropertyContainer::deserialize);

            Customization customization = ParseUtils.parseObject(root, "customization", customizationRoot -> {
                Map<String, PropertyKey<?>> properties = ParseUtils.parseObject(customizationRoot, "properties", PropertyKey::parseProperties);

                List<CustomizationCategory> categories = ParseUtils.parseObject(customizationRoot, "categories", categoryRoot -> {
                    WidgetParseHandler widgetParseHandler = new WidgetParseHandler(properties, new PropertyJsonValueParser.Container(constants));
                    return CustomizationCategory.parseCategories(widgetParseHandler, categoryRoot);
                });

                return new Customization(properties, categories);
            });

            CoordinateSystem coordinateSystem = ParseUtils.parseObject(root, "coordinate_system", systemRoot -> {
                Map<String, ParsableInstanceObject<CoordinateState>> states = ParseUtils.parseObject(systemRoot, "states", JsonTerrariumGenerator::parseCoordinateStates);

                String navigationalStateKey = ParseUtils.getString(systemRoot, "navigational_state");
                if (!states.containsKey(navigationalStateKey)) {
                    ParseStateHandler.error("Could not find navigational coordinate state with key \"" + navigationalStateKey + "\"");
                    navigationalStateKey = CoordinateState.DEFAULT_KEY;
                }

                SpawnpointDefinition spawnpointDefinition = ParseUtils.parseObject(systemRoot, "spawnpoint", spawnpointRoot -> {
                    return JsonTerrariumGenerator.parseSpawnpoint(spawnpointRoot, customization.properties);
                });

                ParsableInstanceObject<Geocoder> geocoder = ParseUtils.parseObject(systemRoot, "geocoder", geocoderRoot -> {
                    ResourceLocation geocoderKey = new ResourceLocation(ParseUtils.getString(geocoderRoot, "type"));
                    InstanceObjectParser<Geocoder> parser = GeocoderRegistry.getGeocoder(geocoderKey);
                    if (parser == null) {
                        throw new InvalidJsonException("Geocoder " + geocoderKey + " does not exist");
                    }
                    return new ParsableInstanceObject<>("geocoder", ParseStateHandler.get().getCurrentLocation(), parser, geocoderRoot);
                });

                return new CoordinateSystem(states, navigationalStateKey, spawnpointDefinition, geocoder);
            });

            DataSystem dataSystem = ParseUtils.parseObject(root, "data_system", dataSystemRoot -> {
                List<AttachedComponent.Parsable<?>> componentParsers = ParseUtils.parseArray(dataSystemRoot, "components", JsonTerrariumGenerator::parseComponents);
                List<ParsableInstanceObject<RegionAdapter>> adapterParsers = ParseUtils.parseArray(dataSystemRoot, "adapters", JsonTerrariumGenerator::parseAdapters);

                return new DataSystem(componentParsers, adapterParsers);
            });

            Composers composers = ParseUtils.parseObject(root, "chunk_composer", composerRoot -> {
                List<ParsableInstanceObject<SurfaceComposer>> surfaceComposers = ParseUtils.parseArray(composerRoot, "surface_composers", JsonTerrariumGenerator::parseSurfaceComposers);
                List<ParsableInstanceObject<DecorationComposer>> decorationComposers = ParseUtils.parseArray(composerRoot, "decoration_composers", JsonTerrariumGenerator::parseDecorationComposers);

                ParsableInstanceObject<BiomeComposer> biomeComposer = ParseUtils.parseObject(composerRoot, "biome_composer", JsonTerrariumGenerator::parseBiomeComposer);

                return new Composers(surfaceComposers, decorationComposers, biomeComposer);
            });

            return new JsonTerrariumGenerator(
                    identifier, customization.categories,
                    coordinateSystem.states, dataSystem.componentParsers, dataSystem.adapterParsers,
                    coordinateSystem.navigationalState, coordinateSystem.spawnpointDefinition, coordinateSystem.geocoder,
                    composers.surface, composers.decoration, composers.biome,
                    constants);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new JsonSyntaxException(e);
        } catch (InvalidJsonException e) {
            ParseStateHandler.error(e);
            throw e;
        } finally {
            ParseStateHandler.popContext();
            ParseStateHandler.finish("parse generator \"" + identifier + "\"");
        }
    }

    private static Map<String, ParsableInstanceObject<CoordinateState>> parseCoordinateStates(JsonObject statesRoot) {
        ParseStateHandler.pushContext("parsing coordinate states");

        Map<String, ParsableInstanceObject<CoordinateState>> coordinateStates = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : statesRoot.entrySet()) {
            ParseStateHandler.pushContext("parsing coordinate state \"" + entry.getKey() + "\"");

            if (entry.getValue().isJsonObject()) {
                JsonObject stateRoot = entry.getValue().getAsJsonObject();

                try {
                    ResourceLocation stateType = new ResourceLocation(ParseUtils.getString(stateRoot, "type"));
                    InstanceObjectParser<CoordinateState> stateParser = CoordinateStateRegistry.get(stateType);

                    if (stateParser != null) {
                        coordinateStates.put(entry.getKey(), new ParsableInstanceObject<>("coordinate state", ParseStateHandler.get().getCurrentLocation(), stateParser, stateRoot));
                    } else {
                        ParseStateHandler.error("Found invalid coordinate state type \"" + stateType + "\"");
                    }
                } catch (InvalidJsonException e) {
                    ParseStateHandler.error(e);
                }
            } else {
                ParseStateHandler.error("Found invalid coordinate state element");
            }

            ParseStateHandler.popContext();
        }

        ParseStateHandler.popContext();
        return coordinateStates;
    }

    private static List<AttachedComponent.Parsable<?>> parseComponents(JsonArray componentsArray) {
        ParseStateHandler.pushContext("parsing region components");

        List<AttachedComponent.Parsable<?>> components = new ArrayList<>();

        for (JsonElement element : componentsArray) {
            if (element.isJsonObject()) {
                JsonObject componentRoot = element.getAsJsonObject();

                try {
                    ResourceLocation typeIdentifier = new ResourceLocation(ParseUtils.getString(componentRoot, "type"));
                    RegionComponentType<?> componentType = DataPipelineRegistries.getComponentType(typeIdentifier);

                    if (componentType != null) {
                        components.add(JsonTerrariumGenerator.parseComponent(componentRoot, componentType));
                    } else {
                        ParseStateHandler.error("Found invalid component type \"" + typeIdentifier + "\"");
                    }
                } catch (InvalidJsonException e) {
                    ParseStateHandler.error(e);
                }
            } else {
                ParseStateHandler.error("Ignored invalid component element \"" + element + "\"");
            }
        }

        ParseStateHandler.popContext();

        return components;
    }

    @SuppressWarnings("unchecked")
    private static <T> AttachedComponent.Parsable<T> parseComponent(JsonObject componentRoot, RegionComponentType<T> componentType) throws InvalidJsonException {
        return ParseUtils.parseObject(componentRoot, "populator", populatorRoot -> {
            ResourceLocation populatorType = new ResourceLocation(ParseUtils.getString(populatorRoot, "type"));
            InstanceObjectParser<? extends RegionPopulator<?>> populatorParser = DataPipelineRegistries.getPopulator(populatorType);

            if (populatorParser == null) {
                throw new InvalidJsonException("Found invalid populator type \"" + populatorType + "\"");
            }

            InstanceObjectParser<RegionPopulator<T>> castedParser = (InstanceObjectParser<RegionPopulator<T>>) populatorParser;
            ParsableInstanceObject<RegionPopulator<T>> parsablePopulator = new ParsableInstanceObject<>("populator", ParseStateHandler.get().getCurrentLocation(), castedParser, populatorRoot);
            return new AttachedComponent.Parsable<>(componentType, parsablePopulator);
        });
    }

    private static List<ParsableInstanceObject<RegionAdapter>> parseAdapters(JsonArray adaptersArray) {
        ParseStateHandler.pushContext("parsing adapters");

        List<ParsableInstanceObject<RegionAdapter>> adapters = new ArrayList<>();

        for (JsonElement element : adaptersArray) {
            if (element.isJsonObject()) {
                JsonObject adapterRoot = element.getAsJsonObject();

                try {
                    ResourceLocation typeIdentifier = new ResourceLocation(ParseUtils.getString(adapterRoot, "type"));
                    InstanceObjectParser<RegionAdapter> adapterParser = DataPipelineRegistries.getAdapter(typeIdentifier);

                    if (adapterParser != null) {
                        adapters.add(new ParsableInstanceObject<>("adapter", ParseStateHandler.get().getCurrentLocation(), adapterParser, adapterRoot));
                    } else {
                        ParseStateHandler.error("Found invalid adapter type \"" + typeIdentifier + "\"");
                    }
                } catch (InvalidJsonException e) {
                    ParseStateHandler.error(e);
                }
            } else {
                ParseStateHandler.error("Ignored invalid adapter element \"" + element + "\"");
            }
        }

        ParseStateHandler.popContext();

        return adapters;
    }

    @SuppressWarnings("unchecked")
    private static SpawnpointDefinition parseSpawnpoint(JsonObject spawnpointRoot, Map<String, PropertyKey<?>> properties) throws InvalidJsonException {
        try {
            ParseStateHandler.pushContext("parsing spawnpoint");

            String propertyXKey = ParseUtils.getString(spawnpointRoot, "x");
            PropertyKey<?> propertyX = properties.get(propertyXKey);
            if (propertyX == null || propertyX.getType() != Number.class) {
                throw new InvalidJsonException("Invalid property \"" + propertyXKey + "\" for spawnpoint x");
            }

            String propertyZKey = ParseUtils.getString(spawnpointRoot, "z");
            PropertyKey<?> propertyZ = properties.get(propertyZKey);
            if (propertyZ == null || propertyZ.getType() != Number.class) {
                throw new InvalidJsonException("Invalid property \"" + propertyZKey + "\" for spawnpoint z");
            }

            return new SpawnpointDefinition((PropertyKey<Number>) propertyX, (PropertyKey<Number>) propertyZ);
        } finally {
            ParseStateHandler.popContext();
        }
    }

    private static List<ParsableInstanceObject<SurfaceComposer>> parseSurfaceComposers(JsonArray composerArray) {
        ParseStateHandler.pushContext("parsing surface composers");

        List<ParsableInstanceObject<SurfaceComposer>> composers = new ArrayList<>();
        for (JsonElement element : composerArray) {
            if (element.isJsonObject()) {
                JsonObject composerRoot = element.getAsJsonObject();

                try {
                    ResourceLocation typeIdentifier = new ResourceLocation(ParseUtils.getString(composerRoot, "type"));
                    InstanceObjectParser<SurfaceComposer> composerParser = ComposerRegistries.getSurfaceComposer(typeIdentifier);

                    if (composerParser != null) {
                        composers.add(new ParsableInstanceObject<>("surface composer", ParseStateHandler.get().getCurrentLocation(), composerParser, composerRoot));
                    } else {
                        ParseStateHandler.error("Found invalid surface composer type: \"" + typeIdentifier + "\"");
                    }
                } catch (InvalidJsonException e) {
                    ParseStateHandler.error(e);
                }
            } else {
                ParseStateHandler.error("Ignored invalid surface composers element");
            }
        }

        ParseStateHandler.popContext();

        return composers;
    }

    private static List<ParsableInstanceObject<DecorationComposer>> parseDecorationComposers(JsonArray composerArray) {
        ParseStateHandler.pushContext("parsing decoration composers");

        List<ParsableInstanceObject<DecorationComposer>> composers = new ArrayList<>();
        for (JsonElement element : composerArray) {
            if (element.isJsonObject()) {
                JsonObject composerRoot = element.getAsJsonObject();

                try {
                    ResourceLocation typeIdentifier = new ResourceLocation(ParseUtils.getString(composerRoot, "type"));
                    InstanceObjectParser<DecorationComposer> composerParser = ComposerRegistries.getDecorationComposer(typeIdentifier);

                    if (composerParser != null) {
                        composers.add(new ParsableInstanceObject<>("decoration composer", ParseStateHandler.get().getCurrentLocation(), composerParser, composerRoot));
                    } else {
                        ParseStateHandler.error("Found invalid decoration composer type: \"" + typeIdentifier + "\"");
                    }
                } catch (InvalidJsonException e) {
                    ParseStateHandler.error(e);
                }
            } else {
                ParseStateHandler.error("Ignored invalid decoration composers element");
            }
        }

        ParseStateHandler.popContext();

        return composers;
    }

    private static ParsableInstanceObject<BiomeComposer> parseBiomeComposer(JsonObject composerRoot) throws InvalidJsonException {
        ResourceLocation typeIdentifier = new ResourceLocation(ParseUtils.getString(composerRoot, "type"));
        InstanceObjectParser<BiomeComposer> composerParser = ComposerRegistries.getBiomeComposer(typeIdentifier);

        if (composerParser != null) {
            return new ParsableInstanceObject<>("biome composer", ParseStateHandler.get().getCurrentLocation(), composerParser, composerRoot);
        } else {
            throw new InvalidJsonException("Found invalid biome composer type " + typeIdentifier);
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
    public RegionDataSystem buildDataSystem(TerrariumWorldData worldData, World world) throws InvalidJsonException {
        RegionDataSystem.Builder builder = RegionDataSystem.builder();

        InstanceJsonValueParser valueParser = new InstanceJsonValueParser(worldData, world, worldData.getSettings().getProperties(), this.constants);

        for (AttachedComponent.Parsable<?> attachedComponent : this.components) {
            builder = builder.withParsableComponent(worldData, world, valueParser, attachedComponent);
        }

        for (ParsableInstanceObject<RegionAdapter> adapter : this.adapters) {
            try {
                builder = builder.withAdapter(adapter.parse(worldData, world, valueParser));
            } catch (InvalidJsonException e) {
                ParseStateHandler.error(e);
            }
        }

        return builder.build();
    }

    @Override
    public Map<String, CoordinateState> buildCoordinateStates(TerrariumWorldData worldData, World world) throws InvalidJsonException {
        Map<String, CoordinateState> entries = new HashMap<>();
        entries.put(CoordinateState.DEFAULT_KEY, new ScaledCoordinateState(1.0, 1.0));

        InstanceJsonValueParser valueParser = new InstanceJsonValueParser(worldData, world, worldData.getSettings().getProperties(), this.constants);

        for (Map.Entry<String, ParsableInstanceObject<CoordinateState>> entry : this.coordinateStates.entrySet()) {
            try {
                entries.put(entry.getKey(), entry.getValue().parse(worldData, world, valueParser));
            } catch (InvalidJsonException e) {
                ParseStateHandler.error(e);
            }
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
    public Geocoder createGeocoder(TerrariumWorldData worldData, World world) throws InvalidJsonException {
        return this.geocoder.parse(worldData, world, this.createParser(worldData, world));
    }

    @Override
    public List<SurfaceComposer> createSurfaceComposers(TerrariumWorldData worldData, World world) throws InvalidJsonException {
        InstanceJsonValueParser valueParser = this.createParser(worldData, world);
        List<SurfaceComposer> composers = new ArrayList<>();
        for (ParsableInstanceObject<SurfaceComposer> parser : this.surfaceComposers) {
            try {
                SurfaceComposer parse = parser.parse(worldData, world, valueParser);
                composers.add(parse);
            } catch (InvalidJsonException e) {
                ParseStateHandler.error(e);
            }
        }
        return composers;
    }

    @Override
    public List<DecorationComposer> createDecorationComposers(TerrariumWorldData worldData, World world) throws InvalidJsonException {
        InstanceJsonValueParser valueParser = this.createParser(worldData, world);
        List<DecorationComposer> composers = new ArrayList<>();
        for (ParsableInstanceObject<DecorationComposer> parser : this.decorationComposers) {
            try {
                DecorationComposer parse = parser.parse(worldData, world, valueParser);
                composers.add(parse);
            } catch (InvalidJsonException e) {
                ParseStateHandler.error(e);
            }
        }
        return composers;
    }

    @Override
    public BiomeComposer createBiomeComposer(TerrariumWorldData worldData, World world) throws InvalidJsonException {
        if (this.biomeComposer != null) {
            InstanceJsonValueParser valueParser = this.createParser(worldData, world);
            return this.biomeComposer.parse(worldData, world, valueParser);
        }
        return null;
    }

    private InstanceJsonValueParser createParser(TerrariumWorldData worldData, World world) {
        return new InstanceJsonValueParser(worldData, world, worldData.getSettings().getProperties(), this.constants);
    }

    private static class Customization {
        private final Map<String, PropertyKey<?>> properties;
        private final List<CustomizationCategory> categories;

        private Customization(Map<String, PropertyKey<?>> properties, List<CustomizationCategory> categories) {
            this.properties = properties;
            this.categories = categories;
        }
    }

    private static class CoordinateSystem {
        private final Map<String, ParsableInstanceObject<CoordinateState>> states;
        private final String navigationalState;
        private final SpawnpointDefinition spawnpointDefinition;
        private final ParsableInstanceObject<Geocoder> geocoder;

        private CoordinateSystem(Map<String, ParsableInstanceObject<CoordinateState>> states, String navigationalState, SpawnpointDefinition spawnpointDefinition, ParsableInstanceObject<Geocoder> geocoder) {
            this.states = states;
            this.navigationalState = navigationalState;
            this.spawnpointDefinition = spawnpointDefinition;
            this.geocoder = geocoder;
        }
    }

    private static class DataSystem {
        private final List<AttachedComponent.Parsable<?>> componentParsers;
        private final List<ParsableInstanceObject<RegionAdapter>> adapterParsers;

        private DataSystem(List<AttachedComponent.Parsable<?>> componentParsers, List<ParsableInstanceObject<RegionAdapter>> adapterParsers) {
            this.componentParsers = componentParsers;
            this.adapterParsers = adapterParsers;
        }
    }

    private static class Composers {
        private final List<ParsableInstanceObject<SurfaceComposer>> surface;
        private final List<ParsableInstanceObject<DecorationComposer>> decoration;
        private final ParsableInstanceObject<BiomeComposer> biome;

        public Composers(List<ParsableInstanceObject<SurfaceComposer>> surface, List<ParsableInstanceObject<DecorationComposer>> decoration, ParsableInstanceObject<BiomeComposer> biome) {
            this.surface = surface;
            this.decoration = decoration;
            this.biome = biome;
        }
    }
}
