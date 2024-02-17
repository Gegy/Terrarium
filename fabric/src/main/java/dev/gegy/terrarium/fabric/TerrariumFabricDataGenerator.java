package dev.gegy.terrarium.fabric;

import dev.gegy.terrarium.registry.TerrariumBiomeClassifiers;
import dev.gegy.terrarium.registry.TerrariumRegistries;
import dev.gegy.terrarium.registry.TerrariumWorldPresets;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

import java.util.concurrent.CompletableFuture;

public class TerrariumFabricDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(final FabricDataGenerator generator) {
        final FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(DynamicRegistryProvider::new);
    }

    @Override
    public void buildRegistry(final RegistrySetBuilder registries) {
        registries.add(Registries.WORLD_PRESET, TerrariumWorldPresets::bootstrap);
        registries.add(TerrariumRegistries.BIOME_CLASSIFIER, TerrariumBiomeClassifiers::bootstrap);
    }

    private static class DynamicRegistryProvider extends FabricDynamicRegistryProvider {
        DynamicRegistryProvider(final FabricDataOutput output, final CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(final HolderLookup.Provider registries, final Entries entries) {
            entries.add(registries.lookupOrThrow(Registries.WORLD_PRESET), TerrariumWorldPresets.EARTH);
            entries.addAll(registries.lookupOrThrow(TerrariumRegistries.BIOME_CLASSIFIER));
        }

        @Override
        public String getName() {
            return "Terrarium Dynamic Registries";
        }
    }
}
