package net.gegy1000.terrarium.server.world.coordinate;

import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyContainer;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;

public class SpawnpointDefinition {
    private final PropertyKey<Number> propertyX;
    private final PropertyKey<Number> propertyZ;

    public SpawnpointDefinition(PropertyKey<Number> propertyX, PropertyKey<Number> propertyZ) {
        this.propertyX = propertyX;
        this.propertyZ = propertyZ;
    }

    public Coordinate createSpawnpoint(GenerationSettings settings, CoordinateState navigationalState) {
        PropertyContainer properties = settings.getProperties();
        double spawnX = properties.getDouble(this.propertyX);
        double spawnZ = properties.getDouble(this.propertyZ);
        return new Coordinate(navigationalState, spawnX, spawnZ);
    }

    public PropertyKey<Number> getPropertyX() {
        return this.propertyX;
    }

    public PropertyKey<Number> getPropertyZ() {
        return this.propertyZ;
    }
}
