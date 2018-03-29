package net.gegy1000.terrarium.server.world.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverRegistry;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyContainer;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyValue;
import net.gegy1000.terrarium.server.world.pipeline.DataPipelineRegistries;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.sampler.DataSampler;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InstanceJsonValueParser extends PropertyJsonValueParser {
    private final PropertyContainer[] propertyContainers;

    protected final TerrariumWorldData worldData;
    protected final World world;

    public InstanceJsonValueParser(TerrariumWorldData worldData, World world, PropertyContainer... propertyContainers) {
        this.worldData = worldData;
        this.world = world;

        this.propertyContainers = propertyContainers;
    }

    @Override
    protected <T> PropertyValue<T> getProperty(String keyIdentifier, Class<T> type) {
        for (PropertyContainer container : this.propertyContainers) {
            if (container.hasKey(keyIdentifier)) {
                PropertyKey<T> key = container.getKey(keyIdentifier, type);
                return container.getValue(key);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> RegionComponentType<T> parseComponentType(JsonObject root, String key, Class<T> type) throws InvalidJsonException {
        ResourceLocation componentKey = new ResourceLocation(ParseUtils.getString(root, key));
        RegionComponentType<?> component = DataPipelineRegistries.getComponentType(componentKey);
        if (component == null) {
            throw new InvalidJsonException("Region component type " + componentKey + " did not exist");
        }
        if (component.getType() != type) {
            throw new InvalidJsonException("Region component " + componentKey + " was not of desired type " + type);
        }
        return (RegionComponentType<T>) component;
    }

    @SuppressWarnings("unchecked")
    public <T extends TiledDataAccess> TiledDataSource<T> parseTiledSource(JsonObject root, String key, Class<T> tileType) throws InvalidJsonException {
        return ParseUtils.parseObject(root, key, objectRoot -> {
            ResourceLocation sourceType = new ResourceLocation(ParseUtils.getString(objectRoot, "type"));
            try {
                ParseStateHandler.pushContext("parsing source with type \"" + sourceType + "\"");

                InstanceObjectParser<TiledDataSource<?>> sourceParser = DataPipelineRegistries.getSource(sourceType);
                if (sourceParser == null) {
                    throw new InvalidJsonException("Source type " + sourceType + " did not exist");
                }
                TiledDataSource<?> source = sourceParser.parse(this.worldData, this.world, this, objectRoot);
                if (source.getTileType() != tileType) {
                    throw new InvalidJsonException("Source " + sourceType + " does not use desired tile type of " + tileType);
                }
                return (TiledDataSource<T>) source;
            } finally {
                ParseStateHandler.popContext();
            }
        });
    }

    public <T> DataSampler<T> parseSampler(JsonObject root, String key, Class<T> dataType) throws InvalidJsonException {
        return ParseUtils.parseObject(root, key, objectRoot -> this.parseSamplerFrom(objectRoot, dataType));
    }

    public <T> List<DataSampler<T>> parseSamplers(JsonObject root, String key, Class<T> dataType) throws InvalidJsonException {
        List<DataSampler<T>> samplers = new ArrayList<>();
        JsonArray array = ParseUtils.getJsonArray(root, key);
        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                try {
                    JsonObject objectRoot = element.getAsJsonObject();
                    samplers.add(this.parseSamplerFrom(objectRoot, dataType));
                } catch (InvalidJsonException e) {
                    ParseStateHandler.error(e);
                }
            } else {
                ParseStateHandler.error("Ignored invalid sampler element");
            }
        }
        return samplers;
    }

    @SuppressWarnings("unchecked")
    private <T> DataSampler<T> parseSamplerFrom(JsonObject objectRoot, Class<T> dataType) throws InvalidJsonException {
        ResourceLocation samplerType = new ResourceLocation(ParseUtils.getString(objectRoot, "type"));
        InstanceObjectParser<DataSampler<?>> samplerParser = DataPipelineRegistries.getSampler(samplerType);
        if (samplerParser == null) {
            throw new InvalidJsonException("Sampler type " + samplerType + " did not exist");
        }
        DataSampler<?> sampler = samplerParser.parse(this.worldData, this.world, this, objectRoot);
        if (sampler.getSamplerType() != dataType) {
            throw new InvalidJsonException("Sampler " + samplerType + " does not use desired type of " + dataType);
        }
        return (DataSampler<T>) sampler;
    }

    public CoordinateState parseCoordinateState(JsonObject root, String key) throws InvalidJsonException {
        String coordinateKey = ParseUtils.getString(root, key);
        CoordinateState state = this.worldData.getCoordinateState(coordinateKey);
        if (state == null) {
            throw new InvalidJsonException("Coordinate state with key " + coordinateKey + " did not exist");
        }
        return state;
    }

    public <T> T parseRegistryEntry(JsonObject root, String key, Map<ResourceLocation, T> registry) throws InvalidJsonException {
        String entryKey = ParseUtils.getString(root, key);
        T entry = registry.get(new ResourceLocation(entryKey));
        if (entry == null) {
            throw new InvalidJsonException("Entry " + entryKey + " did not exist in registry!");
        }
        return entry;
    }

    public CoverGenerationContext parseContext(JsonObject root, String key) throws InvalidJsonException {
        return ParseUtils.parseObject(root, key, contextRoot -> {
            ResourceLocation contextType = new ResourceLocation(ParseUtils.getString(contextRoot, "type"));
            InstanceObjectParser<CoverGenerationContext> parser = CoverRegistry.getContext(contextType);

            return parser.parse(this.worldData, this.world, this, contextRoot);
        });
    }
}
