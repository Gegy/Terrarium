package net.gegy1000.terrarium.server.capability;

import com.google.common.collect.ImmutableMap;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.TerrariumGeneratorRegistry;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.RegionDataSystem;
import net.gegy1000.terrarium.server.world.pipeline.source.Geocoder;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface TerrariumWorldData extends ICapabilityProvider {
    GenerationSettings getSettings();

    RegionDataSystem getDataSystem();

    GenerationRegionHandler getRegionHandler();

    Geocoder getGeocoder();

    Coordinate getSpawnpoint();

    CoordinateState getNavigationalState();

    CoordinateState getCoordinateState(String coordinateKey);

    class Implementation implements TerrariumWorldData {
        private final GenerationSettings settings;
        private final TerrariumGenerator generator;
        private final RegionDataSystem dataSystem;
        private final GenerationRegionHandler regionHandler;
        private final Geocoder geocoder;

        private final ImmutableMap<String, CoordinateState> coordinateStates;
        private final Coordinate spawnpoint;
        private final CoordinateState navigationalState;

        public Implementation(World world) {
            this.settings = GenerationSettings.deserialize(world.getWorldInfo().getGeneratorOptions());
            this.generator = this.settings.getGenerator();

            this.coordinateStates = ImmutableMap.copyOf(this.generator.buildCoordinateStates(this, world));

            String navigationalStateKey = this.generator.getNavigationalStateKey();
            this.navigationalState = this.coordinateStates.get(navigationalStateKey);
            if (this.navigationalState == null) {
                ResourceLocation generatorIdentifier = TerrariumGeneratorRegistry.getIdentifier(this.generator);
                throw new IllegalStateException("Navigational state " + navigationalStateKey + " did not exist on " + generatorIdentifier);
            }

            this.spawnpoint = this.generator.getSpawnpointDefinition().createSpawnpoint(this.settings, this.navigationalState);

            this.dataSystem = this.generator.buildDataSystem(this, world);
            this.geocoder = this.generator.createGeocoder(this, world);
            this.regionHandler = new GenerationRegionHandler(this.settings, this.dataSystem);
        }

        @Override
        public GenerationSettings getSettings() {
            return this.settings;
        }

        @Override
        public RegionDataSystem getDataSystem() {
            return this.dataSystem;
        }

        @Override
        public GenerationRegionHandler getRegionHandler() {
            return this.regionHandler;
        }

        @Override
        public Geocoder getGeocoder() {
            return this.geocoder;
        }

        @Override
        public Coordinate getSpawnpoint() {
            return this.spawnpoint;
        }

        @Override
        public CoordinateState getNavigationalState() {
            return this.navigationalState;
        }

        @Override
        public CoordinateState getCoordinateState(String coordinateKey) {
            return this.coordinateStates.get(coordinateKey);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == TerrariumCapabilities.worldDataCapability;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            if (this.hasCapability(capability, facing)) {
                return TerrariumCapabilities.worldDataCapability.cast(this);
            }
            return null;
        }
    }
}
