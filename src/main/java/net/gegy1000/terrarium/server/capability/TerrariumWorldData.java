package net.gegy1000.terrarium.server.capability;

import com.google.common.base.Strings;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.ChunkCompositionProcedure;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface TerrariumWorldData extends ICapabilityProvider {
    GenerationSettings getSettings();

    GenerationRegionHandler getRegionHandler();

    ChunkCompositionProcedure getCompositionProcedure();

    Coordinate getSpawnPosition();

    class Implementation implements TerrariumWorldData {
        private final GenerationSettings settings;
        private final TerrariumGenerator generator;
        private final GenerationRegionHandler regionHandler;

        public Implementation(World world, TerrariumWorldType worldType) {
            String generatorOptions = world.getWorldInfo().getGeneratorOptions();
            if (Strings.isNullOrEmpty(generatorOptions)) {
                this.settings = worldType.getPreset().createSettings();
            } else {
                this.settings = GenerationSettings.deserialize(generatorOptions);
            }

            this.generator = worldType.buildGenerator(world, this.settings);
            this.regionHandler = new GenerationRegionHandler(this.settings, worldType.buildDataProvider(world, this.settings));
        }

        @Override
        public GenerationSettings getSettings() {
            return this.settings;
        }

        @Override
        public GenerationRegionHandler getRegionHandler() {
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
            if (capability == TerrariumCapabilities.worldDataCapability) {
                return true;
            }
            return this.generator.hasCapability(capability, facing);
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            if (capability == TerrariumCapabilities.worldDataCapability) {
                return TerrariumCapabilities.worldDataCapability.cast(this);
            }
            return this.generator.getCapability(capability, facing);
        }
    }
}
