package net.gegy1000.terrarium.server.capability;

import com.google.common.base.Strings;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.support.SpongeSupport;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.chunk.ComposableChunkGenerator;
import net.gegy1000.terrarium.server.world.chunk.TerrariumChunkGenerator;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.ChunkCompositionProcedure;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface TerrariumWorldData extends ICapabilityProvider {
    ThreadLocal<Boolean> PREVIEW_WORLD = ThreadLocal.withInitial(() -> false);

    GenerationSettings getSettings();

    RegionGenerationHandler getRegionHandler();

    ChunkCompositionProcedure getCompositionProcedure();

    Coordinate getSpawnPosition();

    class Implementation implements TerrariumWorldData {
        private final GenerationSettings settings;
        private final TerrariumGenerator generator;
        private final RegionGenerationHandler regionHandler;

        public Implementation(World world, TerrariumWorldType worldType) {
            String generatorOptions = world.getWorldInfo().getGeneratorOptions();

            GenerationSettings presetSettings = worldType.getPreset().createProperties();
            if (Strings.isNullOrEmpty(generatorOptions)) {
                this.settings = presetSettings;
            } else {
                this.settings = presetSettings.union(GenerationSettings.deserialize(generatorOptions));
            }

            IChunkProvider chunkProvider = world.getChunkProvider();
            if (!(chunkProvider instanceof ChunkProviderServer)) {
                throw new IllegalStateException("Cannot create Terrarium generator with invalid chunk provider " + chunkProvider);
            }

            IChunkGenerator chunkGenerator = ((ChunkProviderServer) chunkProvider).chunkGenerator;
            chunkGenerator = SpongeSupport.unwrapChunkGenerator(chunkGenerator);

            if (!(chunkGenerator instanceof TerrariumChunkGenerator)) {
                Terrarium.LOGGER.warn("Expected generator of type TerrariumChunkGenerator, but got " + chunkGenerator.getClass().getSimpleName());
                chunkGenerator = new ComposableChunkGenerator(world);
            }

            TerrariumGeneratorInitializer initializer = worldType.createInitializer(world, (TerrariumChunkGenerator) chunkGenerator, this.settings);
            this.generator = initializer.buildGenerator(PREVIEW_WORLD.get());
            this.regionHandler = new RegionGenerationHandler(initializer.buildDataProvider());
        }

        @Override
        public GenerationSettings getSettings() {
            return this.settings;
        }

        @Override
        public RegionGenerationHandler getRegionHandler() {
            return this.regionHandler;
        }

        @Override
        public ChunkCompositionProcedure getCompositionProcedure() {
            return this.generator.getCompositionProcedure();
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
