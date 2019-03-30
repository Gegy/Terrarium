package net.gegy1000.terrarium.server.capability;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.ChunkCompositionProcedure;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public interface TerrariumWorldData extends ICapabilityProvider {
    ThreadLocal<Boolean> PREVIEW_WORLD = ThreadLocal.withInitial(() -> false);

    @Nullable
    static TerrariumWorldData get(World world) {
        return world.getCapability(TerrariumCapabilities.worldDataCapability, null);
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
            this.settings = GenerationSettings.parse(world);

            TerrariumGeneratorInitializer initializer = worldType.createInitializer(world, this.settings);
            this.generator = initializer.buildGenerator(PREVIEW_WORLD.get());
            this.regionHandler = new RegionGenerationHandler(world, initializer.buildDataProvider());
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
