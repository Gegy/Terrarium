package net.gegy1000.terrarium.server.world.generator;

import com.google.common.collect.ImmutableList;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.SpawnpointDefinition;
import net.gegy1000.terrarium.server.world.generator.customization.widget.CustomizationCategory;
import net.gegy1000.terrarium.server.world.pipeline.RegionDataSystem;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.source.Geocoder;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

// TODO: Review how a non-json generator would implement this
public interface TerrariumGenerator {
    ImmutableList<CustomizationCategory> getCategories();

    RegionDataSystem buildDataSystem(TerrariumWorldData worldData, World world);

    Map<String, CoordinateState> buildCoordinateStates(TerrariumWorldData worldData, World world);

    String getNavigationalStateKey();

    SpawnpointDefinition getSpawnpointDefinition();

    Geocoder createGeocoder(TerrariumWorldData worldData, World world);

    List<SurfaceComposer> createSurfaceComposers(TerrariumWorldData worldData, World world);

    List<DecorationComposer> createDecorationComposers(TerrariumWorldData worldData, World world);

    BiomeComposer createBiomeComposer(TerrariumWorldData worldData, World world);

    class Default implements TerrariumGenerator {
        @Override
        public ImmutableList<CustomizationCategory> getCategories() {
            throw new UnsupportedOperationException();
        }

        @Override
        public RegionDataSystem buildDataSystem(TerrariumWorldData worldData, World world) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, CoordinateState> buildCoordinateStates(TerrariumWorldData worldData, World world) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getNavigationalStateKey() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SpawnpointDefinition getSpawnpointDefinition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Geocoder createGeocoder(TerrariumWorldData worldData, World world) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<SurfaceComposer> createSurfaceComposers(TerrariumWorldData worldData, World world) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DecorationComposer> createDecorationComposers(TerrariumWorldData worldData, World world) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BiomeComposer createBiomeComposer(TerrariumWorldData worldData, World world) {
            throw new UnsupportedOperationException();
        }
    }
}
