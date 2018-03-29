package net.gegy1000.terrarium.server.world.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.gegy1000.terrarium.Terrarium;
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
import net.minecraft.util.JsonUtils;
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
    public <T> RegionComponentType<T> parseComponentType(JsonObject root, String key, Class<T> type) {
        ResourceLocation componentKey = new ResourceLocation(JsonUtils.getString(root, key));
        RegionComponentType<?> component = DataPipelineRegistries.getComponentType(componentKey);
        if (component == null) {
            throw new JsonSyntaxException("Region component type " + componentKey + " did not exist");
        }
        if (component.getType() != type) {
            throw new JsonSyntaxException("Region component " + componentKey + " was not of desired type " + type);
        }
        return (RegionComponentType<T>) component;
    }

    @SuppressWarnings("unchecked")
    public <T extends TiledDataAccess> TiledDataSource<T> parseTiledSource(JsonObject root, String key, Class<T> tileType) {
        JsonObject objectRoot = TerrariumJsonUtils.parseRemoteObject(root, key);
        ResourceLocation sourceType = new ResourceLocation(JsonUtils.getString(objectRoot, "type"));
        InstanceObjectParser<TiledDataSource<?>> sourceParser = DataPipelineRegistries.getSource(sourceType);
        if (sourceParser == null) {
            throw new JsonSyntaxException("Source type " + sourceType + " did not exist");
        }
        TiledDataSource<?> source = sourceParser.parse(this.worldData, this.world, this, objectRoot);
        if (source.getTileType() != tileType) {
            throw new JsonSyntaxException("Source " + sourceType + " does not use desired tile type of " + tileType);
        }
        return (TiledDataSource<T>) source;
    }

    public <T> DataSampler<T> parseSampler(JsonObject root, String key, Class<T> dataType) {
        JsonObject objectRoot = TerrariumJsonUtils.parseRemoteObject(root, key);
        return this.parseSamplerFrom(objectRoot, dataType);
    }

    public <T> List<DataSampler<T>> parseSamplers(JsonObject root, String key, Class<T> dataType) {
        List<DataSampler<T>> samplers = new ArrayList<>();
        JsonArray array = JsonUtils.getJsonArray(root, key);
        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                JsonObject objectRoot = element.getAsJsonObject();
                samplers.add(this.parseSamplerFrom(objectRoot, dataType));
            } else {
                Terrarium.LOGGER.warn("Ignored non-object sampler {}", element);
            }
        }
        return samplers;
    }

    @SuppressWarnings("unchecked")
    private <T> DataSampler<T> parseSamplerFrom(JsonObject objectRoot, Class<T> dataType) {
        ResourceLocation samplerType = new ResourceLocation(JsonUtils.getString(objectRoot, "type"));
        InstanceObjectParser<DataSampler<?>> samplerParser = DataPipelineRegistries.getSampler(samplerType);
        if (samplerParser == null) {
            throw new JsonSyntaxException("Sampler type " + samplerType + " did not exist");
        }
        DataSampler<?> sampler = samplerParser.parse(this.worldData, this.world, this, objectRoot);
        if (sampler.getSamplerType() != dataType) {
            throw new JsonSyntaxException("Sampler " + samplerType + " does not use desired type of " + dataType);
        }
        return (DataSampler<T>) sampler;
    }

    public CoordinateState parseCoordinateState(JsonObject root, String key) {
        String coordinateKey = JsonUtils.getString(root, key);
        CoordinateState state = this.worldData.getCoordinateState(coordinateKey);
        if (state == null) {
            throw new IllegalStateException("Coordinate state with key " + coordinateKey + " does not exist");
        }
        return state;
    }

    public <T> T parseRegistryEntry(JsonObject root, String key, Map<ResourceLocation, T> registry) {
        String entryKey = JsonUtils.getString(root, key);
        T entry = registry.get(new ResourceLocation(entryKey));
        if (entry == null) {
            throw new JsonSyntaxException("Entry " + entryKey + " does not exist in registry!");
        }
        return entry;
    }

    public CoverGenerationContext parseContext(JsonObject root, String key) {
        JsonObject contextRoot = JsonUtils.getJsonObject(root, key);
        ResourceLocation contextType = new ResourceLocation(JsonUtils.getString(contextRoot, "type"));
        InstanceObjectParser<CoverGenerationContext> parser = CoverRegistry.getContext(contextType);

        return parser.parse(this.worldData, this.world, this, contextRoot);
    }
}
