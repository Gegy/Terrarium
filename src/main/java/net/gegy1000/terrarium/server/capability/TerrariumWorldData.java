package net.gegy1000.terrarium.server.capability;

import com.google.common.base.Strings;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.chunk.TerrariumChunkDelegate;
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
import net.minecraftforge.fml.common.Loader;

public interface TerrariumWorldData extends ICapabilityProvider {
    ThreadLocal<Boolean> PREVIEW_WORLD = ThreadLocal.withInitial(() -> false);

    static TerrariumWorldData get(World world) {
        TerrariumWorldData worldData = world.getCapability(TerrariumCapabilities.worldDataCapability, null);
        if (worldData == null) {
            throw new IllegalStateException("Terrarium world capability not yet present");
        }
        return worldData;
    }

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

            TerrariumChunkDelegate delegate = getDelegate(world);

            TerrariumGeneratorInitializer initializer = worldType.createInitializer(world, delegate, this.settings);
            this.generator = initializer.buildGenerator(PREVIEW_WORLD.get());
            this.regionHandler = new RegionGenerationHandler(initializer.buildDataProvider());
        }

        private static TerrariumChunkDelegate getDelegate(World world) {
            IChunkProvider provider = world.getChunkProvider();
            if (Loader.isModLoaded("cubicchunks") && provider instanceof CubeProviderServer) {
                // TODO: Classload
                ICubeGenerator generator = ((CubeProviderServer) provider).getCubeGenerator();
                if (generator instanceof TerrariumChunkDelegate) {
                    return (TerrariumChunkDelegate) generator;
                }
            } else if (provider instanceof ChunkProviderServer) {
                IChunkGenerator generator = ((ChunkProviderServer) provider).chunkGenerator;
                if (generator instanceof TerrariumChunkDelegate) {
                    return (TerrariumChunkDelegate) generator;
                }
            }
            throw new IllegalStateException("Unable to retrieve chunk generator delegate for Terrarium world");
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
