package net.gegy1000.terrarium.server.capability;

import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.gegy1000.terrarium.server.world.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.ColumnDataGenerator;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public interface TerrariumWorld extends ICapabilityProvider {
    ThreadLocal<Boolean> PREVIEW_WORLD = ThreadLocal.withInitial(() -> false);

    @Nullable
    static TerrariumWorld get(World world) {
        return world.getCapability(TerrariumCapabilities.worldDataCapability, null);
    }

    GenerationSettings getSettings();

    ColumnDataCache getDataCache();

    SurfaceComposer getSurfaceComposer();

    DecorationComposer getDecorationComposer();

    StructureComposer getStructureComposer();

    BiomeComposer getBiomeComposer();

    Coordinate getSpawnPosition();

    class Impl implements TerrariumWorld {
        private final GenerationSettings settings;
        private final TerrariumGenerator generator;
        private final ColumnDataCache dataCache;

        public Impl(World world, TerrariumWorldType worldType) {
            this.settings = GenerationSettings.parse(world);

            TerrariumGeneratorInitializer generatorInitializer = worldType.createGeneratorInitializer(world, this.settings);
            TerrariumDataInitializer dataInitializer = worldType.createDataInitializer(world, this.settings);

            this.generator = generatorInitializer.buildGenerator(PREVIEW_WORLD.get());

            ColumnDataGenerator dataGenerator = dataInitializer.buildDataGenerator();
            this.dataCache = new ColumnDataCache(world, dataGenerator);
        }

        @Override
        public GenerationSettings getSettings() {
            return this.settings;
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

        @Override
        public Coordinate getSpawnPosition() {
            return this.generator.getSpawnPosition();
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == TerrariumCapabilities.worldDataCapability;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            if (capability == TerrariumCapabilities.worldDataCapability) {
                return TerrariumCapabilities.worldDataCapability.cast(this);
            }
            return null;
        }
    }
}
