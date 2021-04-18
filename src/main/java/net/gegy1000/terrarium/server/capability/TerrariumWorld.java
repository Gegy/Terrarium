package net.gegy1000.terrarium.server.capability;

import net.gegy1000.terrarium.server.event.TerrariumInitializeDataEvent;
import net.gegy1000.terrarium.server.event.TerrariumInitializeGeneratorEvent;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.composer.RoughHeightmapComposer;
import net.gegy1000.terrarium.server.world.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.gegy1000.terrarium.server.world.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataGenerator;
import net.gegy1000.terrarium.server.world.generator.CompositeTerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public interface TerrariumWorld extends ICapabilityProvider {
    @Nullable
    static TerrariumWorld get(World world) {
        return world.getCapability(TerrariumCapabilities.world(), null);
    }

    World getWorld();

    GenerationSettings getSettings();

    DataGenerator getDataGenerator();

    ColumnDataCache getDataCache();

    SurfaceComposer getSurfaceComposer();

    DecorationComposer getDecorationComposer();

    StructureComposer getStructureComposer();

    BiomeComposer getBiomeComposer();

    @Nullable
    RoughHeightmapComposer getRoughHeightmapComposer();

    Coordinate getSpawnPosition();

    @Override
    default boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == TerrariumCapabilities.world();
    }

    @Override
    default <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == TerrariumCapabilities.world() ? TerrariumCapabilities.world().cast(this) : null;
    }

    class Impl implements TerrariumWorld {
        private final World world;
        private final GenerationSettings settings;
        private final TerrariumGenerator generator;
        private final DataGenerator dataGenerator;
        private final ColumnDataCache dataCache;

        public Impl(WorldServer world, TerrariumWorldType worldType) {
            this.world = world;
            this.settings = GenerationSettings.parseFrom(world);

            DataGenerator.Builder dataGenerator = DataGenerator.builder();

            TerrariumDataInitializer dataInitializer = worldType.createDataInitializer(this.settings);
            dataInitializer.setup(dataGenerator);

            MinecraftForge.EVENT_BUS.post(new TerrariumInitializeDataEvent(world, worldType, this.settings, dataGenerator));

            this.dataGenerator = dataGenerator.build();
            this.dataCache = new ColumnDataCache(world, this.dataGenerator);

            CompositeTerrariumGenerator.Builder generator = CompositeTerrariumGenerator.builder();

            TerrariumGeneratorInitializer generatorInitializer = worldType.createGeneratorInitializer(world, this.settings, this.dataCache);
            generatorInitializer.setup(generator);

            MinecraftForge.EVENT_BUS.post(new TerrariumInitializeGeneratorEvent(world, worldType, this.settings, generator, this.dataCache));

            this.generator = generator.build();
        }

        @Override
        public World getWorld() {
            return this.world;
        }

        @Override
        public GenerationSettings getSettings() {
            return this.settings;
        }

        @Override
        public DataGenerator getDataGenerator() {
            return this.dataGenerator;
        }

        @Override
        public ColumnDataCache getDataCache() {
            return this.dataCache;
        }

        @Override
        public SurfaceComposer getSurfaceComposer() {
            return this.generator.getSurfaceComposer();
        }

        @Override
        public DecorationComposer getDecorationComposer() {
            return this.generator.getDecorationComposer();
        }

        @Override
        public StructureComposer getStructureComposer() {
            return this.generator.getStructureComposer();
        }

        @Override
        public BiomeComposer getBiomeComposer() {
            return this.generator.getBiomeComposer();
        }

        @Nullable
        @Override
        public RoughHeightmapComposer getRoughHeightmapComposer() {
            return this.generator.getRoughHeightmapComposer();
        }

        @Override
        public Coordinate getSpawnPosition() {
            return this.generator.getSpawnPosition();
        }
    }
}
