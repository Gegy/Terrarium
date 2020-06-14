package net.gegy1000.terrarium.server.world.composer.biome;

import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;

public interface BiomeComposer {
    void composeBiomes(Biome[] buffer, TerrariumWorld terrarium, ColumnData data, DataView view);

    final class Default implements BiomeComposer {
        public static final BiomeComposer INSTANCE = new Default();

        private Default() {
        }

        @Override
        public void composeBiomes(Biome[] buffer, TerrariumWorld terrarium, ColumnData data, DataView view) {
            Arrays.fill(buffer, Biomes.DEFAULT);
        }
    }
}
